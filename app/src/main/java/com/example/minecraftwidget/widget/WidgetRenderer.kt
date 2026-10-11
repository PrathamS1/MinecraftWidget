package com.example.minecraftwidget.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import com.example.minecraftwidget.R
import com.example.minecraftwidget.catalog.CatalogRepository
import com.example.minecraftwidget.catalog.SpriteSet
import com.example.minecraftwidget.data.SpriteFetch
import com.example.minecraftwidget.data.Fill
import com.example.minecraftwidget.data.SpriteSource
import com.example.minecraftwidget.data.BackgroundShape
import com.example.minecraftwidget.data.TapAction
import com.example.minecraftwidget.data.WidgetConfig
import com.example.minecraftwidget.data.WidgetConfigStore
import com.example.minecraftwidget.sprite.BitmapOps
import com.example.minecraftwidget.sprite.DecodedSprite
import com.example.minecraftwidget.sprite.FramePlanner
import com.example.minecraftwidget.sprite.IdleAnimator
import com.example.minecraftwidget.sprite.IdleFrame
import com.example.minecraftwidget.sprite.IdleStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

/**
 * Turns a [WidgetConfig] into the `RemoteViews` the launcher displays.
 *
 * The animation itself is a `ViewFlipper` running inside the launcher's own process:
 * once the frames are handed over, nothing of ours runs again until the user changes
 * something. No alarms, no services, no wakelocks, and the launcher stops the flipping
 * on its own when the widget scrolls off screen.
 */
class WidgetRenderer(private val context: Context) {

    private val catalog = CatalogRepository.get(context)
    private val source = SpriteSource(context)
    private val configStore = WidgetConfigStore(context)

    /**
     * Renders one widget. Safe to call from anywhere, including the main thread.
     *
     * The dispatcher is chosen here rather than left to the caller because the work is
     * genuinely heavy and the callers do not look like they are asking for heavy work.
     * A Black/White sprite is up to 160 GIF frames: decoding them, scanning every pixel of
     * every frame for the opaque bounds and then hashing each one for the dedupe pass adds
     * up to hundreds of milliseconds. The widget provider already ran this on
     * [Dispatchers.Default], but `ConfigViewModel.save` calls it from `viewModelScope` —
     * which is the main thread — so tapping "Add to home screen" froze the UI for as long
     * as the chosen sprite was expensive.
     */
    suspend fun render(widgetId: Int) = withContext(Dispatchers.Default) {
        val manager = AppWidgetManager.getInstance(context)
        val config = resolveConfig(widgetId)
        val options = runCatching { manager.getAppWidgetOptions(widgetId) }.getOrNull()

        val views = try {
            buildViews(widgetId, config, options)
        } catch (e: Exception) {
            Log.e(TAG, "failed to build widget $widgetId", e)
            statusViews(widgetId, config, "Couldn't load sprite")
        }
        updateSafely(manager, widgetId, views, config)
        warmCry(config)
    }

    /**
     * Downloads and decodes the cry now, while we are already doing network work and are
     * under no deadline, so that the first tap does not have to.
     *
     * Done whenever cries are on, whatever the tap action is today: the action can be
     * switched to "Play cry" later, and that change should not bring back a slow first tap.
     * Goes through [CryPlayer] so a render and a tap racing for the same cry share one
     * download. Failure here is fine and ignored: the tap path still fetches for itself.
     */
    private suspend fun warmCry(config: WidgetConfig) {
        if (!config.cryEnabled) return
        runCatching { CryPlayer.preload(context, config.entityId, config.legacyCry) }
            .onFailure { Log.d(TAG, "could not pre-fetch a cry for " + config.entityId, it) }
    }

    /**
     * Settings for this widget, adopting the user's in-app choice if this is a brand new
     * widget they just asked the launcher to place.
     *
     * `requestPinAppWidget` carries no payload and does not run the configuration
     * activity, so without this every "Add Charizard to home screen" would land as the
     * default Pikachu. Adopting the choice here, rather than only in the pin callback,
     * means it survives whichever route the widget's first render arrives by — the
     * callback, a resize, or the config activity.
     */
    private suspend fun resolveConfig(widgetId: Int): WidgetConfig {
        return if (configStore.exists(widgetId)) {
            configStore.get(widgetId)
        } else {
            configStore.takePendingPin()
                ?.also { configStore.put(widgetId, it) }
                ?: configStore.get(widgetId)
        }
    }

    /**
     * A widget update that busts the system's bitmap ceiling throws, and an uncaught
     * throw here takes the launcher down with it. The planner budgets conservatively so
     * this should never fire — but "should never" is not a thing to bet someone's home
     * screen on, so re-plan against a halved budget and fall back to a still frame.
     *
     * The net is RuntimeException, not IllegalArgumentException. The bitmap ceiling is
     * only one of the ways updateAppWidget can reject a payload — a recycled bitmap
     * raises IllegalStateException, an oversized parcel a TransactionTooLargeException —
     * and every one of those used to escape to WidgetUpdater's runCatching, where it was
     * logged and forgotten while the widget sat on its placeholder forever. Anything that
     * reaches here should end as a visible message, never as a blank square.
     */
    private suspend fun updateSafely(
        manager: AppWidgetManager,
        widgetId: Int,
        views: RemoteViews,
        config: WidgetConfig,
    ) {
        try {
            manager.updateAppWidget(widgetId, views)
        } catch (e: RuntimeException) {
            Log.w(TAG, "widget $widgetId was rejected by the launcher; retrying smaller", e)
            val retry = runCatching {
                buildViews(widgetId, config, manager.getAppWidgetOptions(widgetId), budgetScale = 0.35)
            }.getOrNull() ?: statusViews(widgetId, config, "Sprite too large")
            runCatching { manager.updateAppWidget(widgetId, retry) }
                .onFailure { Log.e(TAG, "widget $widgetId could not be updated at all", it) }
        }
    }

    private suspend fun buildViews(
        widgetId: Int,
        config: WidgetConfig,
        options: Bundle?,
        budgetScale: Double = 1.0,
    ): RemoteViews {
        val set = catalog.set(config.setId)
            ?: return statusViews(widgetId, config, "Sprite set unavailable")

        // Missing and Offline mean opposite things to the person looking at the widget: one
        // is "pick a different set", the other is "try again in a minute". Saying the wrong
        // one is how a widget ends up being tapped forever with no hope of recovering.
        val parts = when (val fetched = source.spriteParts(set, config.spriteKey)) {
            is SpriteFetch.Ok -> fetched.parts
            SpriteFetch.Missing -> return statusViews(
                widgetId, config, "${set.label} never drew this one",
            )
            SpriteFetch.Offline -> return statusViews(
                widgetId,
                config,
                if (source.isSpriteCached(config.spriteKey, set.ext)) {
                    "Couldn't read sprite"
                } else {
                    "Tap to retry — no connection"
                },
            )
        }

                val actualIsGif = set.ext == "gif" || (parts.firstOrNull()?.let { it.size > 3 && it[0] == 'G'.code.toByte() && it[1] == 'I'.code.toByte() && it[2] == 'F'.code.toByte() } == true)
        if (actualIsGif) {
            return statusViews(widgetId, config, "GIFs are not supported")
        }

        val decoded = try {
            val frames = mutableListOf<android.graphics.Bitmap>()
            val delays = mutableListOf<Int>()
            for (part in parts) {
                val apng = com.example.minecraftwidget.sprite.ApngFrames.decode(part)
                if (apng != null) {
                    frames.addAll(apng.frames)
                    delays.addAll(apng.delaysMs)
                } else {
                    val bmp = android.graphics.BitmapFactory.decodeByteArray(part, 0, part.size)
                    if (bmp != null) {
                        if (set.frameCount != null && set.frameWidth != null && set.frameHeight != null && set.frameCount > 1) {
                            val cols = bmp.width / set.frameWidth
                            val rows = bmp.height / set.frameHeight
                            var count = 0
                            for (r in 0 until rows) {
                                for (c in 0 until cols) {
                                    if (count >= set.frameCount) break
                                    val frameBmp = android.graphics.Bitmap.createBitmap(bmp, c * set.frameWidth, r * set.frameHeight, set.frameWidth, set.frameHeight)
                                    frames.add(frameBmp)
                                    delays.add(100)
                                    count++
                                }
                            }
                            bmp.recycle()
                        } else {
                            frames.add(bmp)
                            delays.add(100)
                        }
                    }
                }
            }
            if (frames.isNotEmpty()) {
                // frameDelaysMs defaults to an empty list (not null), so `?:` alone never falls
                // back — sprite sheets reached the planner with no delays and it threw.
                val finalDelays = set.frameDelaysMs.takeIf { it.size >= frames.size } ?: delays
                DecodedSprite(frames, finalDelays.take(frames.size))
            } else null
        } catch (e: Exception) { null }
            ?: return statusViews(widgetId, config, "Couldn't decode sprite")

        return try {
            composeViews(widgetId, config, set, decoded, options, budgetScale)
        } finally {
            decoded.recycle()
        }
    }

    private fun composeViews(
        widgetId: Int,
        config: WidgetConfig,
        set: SpriteSet,
        decoded: DecodedSprite,
        options: Bundle?,
        budgetScale: Double,
    ): RemoteViews {
        val metrics = context.resources.displayMetrics
        val (boxW, boxH) = contentBoxPx(options, metrics)
        val budget = (
            FramePlanner.budgetFor(metrics.widthPixels, metrics.heightPixels) * budgetScale
            ).toLong().coerceAtLeast(MIN_BUDGET)
        var spriteBudget = budget

        val bounds = BitmapOps.unionOpaqueBounds(decoded.frames)
        val excited = config.excitedUntilMs > System.currentTimeMillis()
        val wantsCustomIdle = config.idleStyle != IdleStyle.AUTO
        val playAnimationOnTap = excited && config.tapAction == TapAction.PLAY_ANIMATION
        val playAnimatedFrames = set.animated && decoded.frames.size > 1 && (!wantsCustomIdle || playAnimationOnTap)
        val activeIdleStyle = if (excited && config.tapAction == TapAction.WOBBLE) IdleStyle.SWAY else config.idleStyle
        val speedUp = if (excited && config.tapAction != TapAction.PLAY_ANIMATION) EXCITED_SPEEDUP else 1.0

        val expectsAnimation = playAnimatedFrames ||
            IdleAnimator.resolve(activeIdleStyle, config.setId) != IdleStyle.NONE
        val layoutId = if (expectsAnimation) R.layout.widget_root_animated else R.layout.widget_root
        val views = RemoteViews(context.packageName, layoutId)
        val radiusPx = config.cornerRadiusDp * metrics.density

        views.setViewVisibility(R.id.widget_status, View.GONE)
        views.setViewVisibility(R.id.widget_flipper, View.VISIBLE)
        views.removeAllViews(R.id.widget_flipper)
        resetScene(views)

        val layout = SceneLayout.layout(boxW, boxH)
        spriteBudget = spriteBudget.coerceAtLeast(MIN_BUDGET)

        val box = layout.entities

        val (shownWidth, shownHeight) = if (playAnimatedFrames) {
            addAnimatedFrames(views, decoded, bounds, config, set, box.width, box.height, spriteBudget, speedUp)
        } else {
            addIdleFrames(views, decoded, bounds, config, set, box.width, box.height, spriteBudget, speedUp, activeIdleStyle)
        }

        if (config.showBackground) {
            // Background directly behind the entity, matched to asset size
            val plateDim = if (config.backgroundShape == BackgroundShape.CIRCLE || config.backgroundShape == BackgroundShape.BLOB) {
                maxOf(shownWidth, shownHeight)
            } else {
                0
            }
            val plateW = if (plateDim > 0) plateDim else shownWidth
            val plateH = if (plateDim > 0) plateDim else shownHeight

            val centerX = (box.left + box.right) / 2
            val centerY = (box.top + box.bottom) / 2

            val bgLeft = (centerX - plateW / 2).coerceIn(0, boxW - 1)
            val bgTop = (centerY - plateH / 2).coerceIn(0, boxH - 1)
            val bgRight = (bgLeft + plateW).coerceAtMost(boxW)
            val bgBottom = (bgTop + plateH).coerceAtMost(boxH)

            val padLeft = bgLeft
            val padTop = bgTop
            val padRight = (boxW - bgRight).coerceAtLeast(0)
            val padBottom = (boxH - bgBottom).coerceAtLeast(0)

            val plate = BitmapOps.shapePlate(
                plateW,
                plateH,
                config.backgroundShape,
                config.backgroundColor,
                radiusPx,
            )
            views.setViewPadding(R.id.widget_background, padLeft, padTop, padRight, padBottom)
            views.setImageViewBitmap(R.id.widget_background, plate)
            views.setViewVisibility(R.id.widget_background, View.VISIBLE)
        } else {
            views.setViewPadding(R.id.widget_background, 0, 0, 0, 0)
            views.setViewVisibility(R.id.widget_background, View.GONE)
        }

        views.setOnClickPendingIntent(R.id.widget_root, tapIntent(widgetId, config))
        return views
    }

    private fun resetScene(views: RemoteViews) {
        views.setViewPadding(R.id.widget_flipper, 0, 0, 0, 0)
        views.setViewPadding(R.id.widget_background, 0, 0, 0, 0)
    }

    /** Screen pixels per source pixel for this widget's size setting. */
    private fun displayScale(
        config: WidgetConfig,
        set: SpriteSet,
        contentW: Int,
        contentH: Int,
        boxW: Int,
        boxH: Int,
    ): Double = FramePlanner.displayScale(
        contentWidth = contentW,
        contentHeight = contentH,
        targetWidthPx = boxW,
        targetHeightPx = boxH,
        multiple = config.fill.multiple,
        referencePx = if (config.fill == Fill.TRUE_SIZE) set.referencePx else null,
    )

    /** The real thing: resampled GIF frames driven by the flipper. */
    private fun addAnimatedFrames(
        views: RemoteViews,
        decoded: DecodedSprite,
        bounds: android.graphics.Rect,
        config: WidgetConfig,
        set: SpriteSet,
        boxW: Int,
        boxH: Int,
        budget: Long,
        speedUp: Double,
    ): Pair<Int, Int> {
        val plan = FramePlanner.plan(
            FramePlanner.Request(
                source = FramePlanner.Source(
                    contentWidth = bounds.width(),
                    contentHeight = bounds.height(),
                    delaysMs = decoded.delaysMs,
                    canonical = BitmapOps.canonicalFrames(decoded.frames, bounds),
                ),
                targetWidthPx = boxW,
                targetHeightPx = boxH,
                displayScale = displayScale(config, set, bounds.width(), bounds.height(), boxW, boxH),
                desiredFps = config.smoothness.fps,
                budgetBytes = budget,
            ),
        )

        // One bitmap per *distinct* source frame, reused across every step that shows it.
        // RemoteViews.BitmapCache dedupes by object identity, so a long-held frame costs
        // one bitmap instead of a dozen — which is what keeps sprites like Crystal's (a
        // 990 ms hold in a 2.7 s loop) inside the budget.
        val stored = HashMap<Int, Bitmap>(plan.distinctFrames.size)
        for (index in plan.distinctFrames) {
            decoded.frames.getOrNull(index)?.let { frame ->
                val bitmap = BitmapOps.cropScaleTo(frame, bounds, plan.outWidth, plan.outHeight)
                stored[index] = if (config.flipHorizontal) BitmapOps.mirrored(bitmap) else bitmap
            }
        }

        // A SCALED plan's bitmaps are smaller than they are shown; a fitCenter ImageView,
        // padded down to exactly the display rectangle, stretches them the rest of the way.
        val sharp = plan.tier == FramePlanner.Tier.SHARP
        val layout = if (sharp) R.layout.widget_frame else R.layout.widget_frame_fit
        val padX = ((boxW - plan.displayWidth) / 2).coerceAtLeast(0)
        val padY = ((boxH - plan.displayHeight) / 2).coerceAtLeast(0)

        for (index in plan.sourceIndices) {
            val bitmap = stored[index] ?: continue
            val child = RemoteViews(context.packageName, layout)
            child.setImageViewBitmap(R.id.widget_frame_image, bitmap)
            if (!sharp) child.setViewPadding(R.id.widget_frame_image, padX, padY, padX, padY)
            views.addView(R.id.widget_flipper, child)
        }

        startFlipping(views, (plan.frameIntervalMs / speedUp).roundToInt())
        Log.d(
            TAG,
            "animated: ${plan.stepCount} steps / ${plan.distinctFrames.size} bitmaps, " +
                "${plan.tier} ${plan.outWidth}x${plan.outHeight} shown at " +
                "${plan.displayWidth}x${plan.displayHeight}, ${plan.fps} fps, " +
                "${plan.estimatedBytes / 1024} KB of $budget",
        )
        return plan.displayWidth to plan.displayHeight
    }

    /**
     * Still sprites — every GBA set including Emerald, all of Gen 4, and everything from
     * Gen 6 on — get a generated idle animation instead of sitting dead on the home
     * screen. See [IdleAnimator] for why those games have no animated sprites to fetch.
     *
     * The cost is one bitmap per distinct *shape* in the loop, not one per step:
     * translation is expressed as padding, and `RemoteViews.BitmapCache` dedupes by
     * object identity, so a ten-step sway is still a single bitmap and a six-step breath
     * is three.
     */
    private fun addIdleFrames(
        views: RemoteViews,
        decoded: DecodedSprite,
        bounds: android.graphics.Rect,
        config: WidgetConfig,
        set: SpriteSet,
        boxW: Int,
        boxH: Int,
        budget: Long,
        speedUp: Double, activeIdleStyle: IdleStyle
    ): Pair<Int, Int> {
        val source = decoded.frames.firstOrNull() ?: return 0 to 0
        // AUTO means "whatever suits this set's artwork" — a squash-and-stretch breath on
        // pixel art, a shape-preserving swell on a 3D render.
        val style = IdleAnimator.resolve(activeIdleStyle, config.setId)
        // Leave room for the widest step, so a breath does not clip at the edges of a
        // snugly-fitted widget.
        val widest = style.frames.maxOfOrNull { it.scaleXPermille } ?: IdleFrame.NATURAL
        val tallest = style.frames.maxOfOrNull { it.scaleYPermille } ?: IdleFrame.NATURAL
        var scale = displayScale(
            config,
            set,
            (bounds.width() * widest / IdleFrame.NATURAL).coerceAtLeast(1),
            (bounds.height() * tallest / IdleFrame.NATURAL).coerceAtLeast(1),
            boxW,
            boxH,
        )
        // Idle shapes are distorted per step, so they cannot be stretched by a fitCenter
        // ImageView without undoing the distortion; they are always stored at display size.
        // One bitmap of that size is small next to any real budget, but a 3D render on a
        // huge widget on a small screen is shrunk until a single one fits.
        while (bounds.width() * bounds.height() * scale * scale * 4 > budget && scale > 0.1) {
            scale *= 0.85
        }
        fun px(v: Int) = (v * scale).roundToInt().coerceAtLeast(1)
        val natural = BitmapOps.cropScaleTo(source, bounds, px(bounds.width()), px(bounds.height()))
            .let { if (config.flipHorizontal) BitmapOps.mirrored(it) else it }

        val frames = style.frames
        val shapes = frames.distinctBy { it.shapeKey }
        val perBitmap = natural.width.toLong() * natural.height * 4L

        // Shape changes are the only thing here that costs memory, so they are the only
        // thing worth giving up. Drop to pure translation, then to a single still frame.
        val affordable = when {
            perBitmap * shapes.size <= budget -> frames
            perBitmap * 2 <= budget -> IdleStyle.BOB.frames
            else -> listOf(IdleFrame())
        }

        val byShape = HashMap<Int, Bitmap>(shapes.size)
        for (frame in affordable) {
            byShape.getOrPut(frame.shapeKey) {
                BitmapOps.resize(
                    natural,
                    natural.width * frame.scaleXPermille / IdleFrame.NATURAL,
                    natural.height * frame.scaleYPermille / IdleFrame.NATURAL,
                )
            }
        }

        // A mirrored sprite sways the other way, so it still leans into its own motion.
        val xSign = if (config.flipHorizontal) -1 else 1
        for (frame in affordable) {
            val bitmap = byShape[frame.shapeKey] ?: natural
            val child = RemoteViews(context.packageName, R.layout.widget_frame)
            child.setImageViewBitmap(R.id.widget_frame_image, bitmap)

            // The frame's ImageView is scaleType="center", so a shorter bitmap would
            // float upward as it squashes. Pushing down by half the height it lost keeps
            // the sprite's feet planted, which is what makes a squash read as weight
            // rather than as the whole creature shrinking.
            val plant = (natural.height - bitmap.height) / 2
            val dy = (frame.dySource * scale).roundToInt() + plant
            val dx = (frame.dxSource * scale).roundToInt() * xSign
            // The equal-and-opposite padding on the far side keeps the content box the
            // same size, so nothing reflows between steps.
            child.setViewPadding(R.id.widget_frame_image, dx, dy, -dx, -dy)
            views.addView(R.id.widget_flipper, child)
        }

        if (affordable.size <= 1) return natural.width to natural.height
        startFlipping(views, (style.frameIntervalMs / speedUp).roundToInt())
        Log.d(
            TAG,
            "idle ${style.name.lowercase()}: ${affordable.size} steps / ${byShape.size} " +
                "bitmaps, ${natural.width}x${natural.height}, ${perBitmap * byShape.size / 1024} KB of $budget",
        )
        return natural.width to natural.height
    }



    /**
     * The one animation lever `RemoteViews` gives us.
     *
     * Only the interval is settable at runtime: `ViewFlipper.setFlipInterval(int)` is a
     * `@RemotableViewMethod`, but `setAutoStart(boolean)` is not — pushing it throws
     * `ActionException: ViewFlipper can't use method with RemoteViews`. Auto-start is
     * therefore declared as `android:autoStart` in widget_root.xml instead.
     */
    private fun startFlipping(views: RemoteViews, intervalMs: Int) {
        views.setInt(R.id.widget_flipper, "setFlipInterval", intervalMs.coerceIn(16, 5_000))
    }

    private fun statusViews(widgetId: Int, config: WidgetConfig, message: String): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_root)
        views.removeAllViews(R.id.widget_flipper)
        views.setViewVisibility(R.id.widget_flipper, View.GONE)
        resetScene(views)
        views.setViewVisibility(R.id.widget_background, View.GONE)
        views.setViewVisibility(R.id.widget_status, View.VISIBLE)
        views.setTextViewText(R.id.widget_status, message)
        views.setOnClickPendingIntent(R.id.widget_root, tapIntent(widgetId, config))
        return views
    }

    private fun tapIntent(widgetId: Int, config: WidgetConfig): PendingIntent {
        val intent = Intent(context, MobWidgetProvider.Medium::class.java).apply {
            action = WidgetActions.ACTION_TAP
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            // Everything the tap needs to start the cry, so it never waits on DataStore.
            TapPayload.of(config).write({ k, v -> putExtra(k, v) }, { k, v -> putExtra(k, v) })
            // A tap is someone waiting for a sound. The background broadcast queue can sit
            // behind other apps' broadcasts; the foreground one is delivered promptly.
            addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
            // The extras are not part of a PendingIntent's identity, so without a unique
            // data URI every widget would end up sharing the first one's intent.
            data = android.net.Uri.parse("minecraftwidget://tap/$widgetId")
        }
        return PendingIntent.getBroadcast(
            context,
            widgetId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    /**
     * The launcher reports the widget's size in dp, and only as a min/max pair. Take the
     * larger figure for each axis so the sprite is planned for the space it will actually
     * occupy in the current orientation.
     */
    private fun contentBoxPx(options: Bundle?, metrics: DisplayMetrics): Pair<Int, Int> {
        // The launcher reports two boxes in one bundle: portrait is MIN_WIDTH × MAX_HEIGHT,
        // landscape is MAX_WIDTH × MIN_HEIGHT. Taking the larger figure on both axes planned
        // a box wider than the widget really is in portrait. A lone centred sprite hid that;
        // a trainer placed by padding, or a background stretched to fit, does not.
        val portrait = context.resources.configuration.orientation !=
            android.content.res.Configuration.ORIENTATION_LANDSCAPE
        fun dp(key: String) = options?.getInt(key, 0)?.takeIf { it > 0 }
        val widthDp = (if (portrait) dp(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH) else dp(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH))
            ?: dp(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH)
            ?: dp(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
            ?: DEFAULT_BOX_DP
        val heightDp = (if (portrait) dp(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT) else dp(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT))
            ?: dp(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)
            ?: dp(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
            ?: DEFAULT_BOX_DP

        val w = (widthDp * metrics.density).toInt().coerceIn(48, metrics.widthPixels)
        val h = (heightDp * metrics.density).toInt().coerceIn(48, metrics.heightPixels)
        Log.d(TAG, "widget box ${widthDp}x${heightDp} dp → ${w}x$h px (${if (portrait) "portrait" else "landscape"})")
        return w to h
    }

    companion object {
        private const val TAG = "WidgetRenderer"
        private const val DEFAULT_BOX_DP = 110
        private const val MIN_BUDGET = 512L * 1024

        /** A battle background never needs more than this; it is stretched, not studied. */
        private const val MAX_BACKGROUND_BYTES = 1_200_000L
        const val EXCITED_SPEEDUP = 2.2

        /** Pushes an update for every placed widget, e.g. after a config change. */
        fun componentNames(context: Context): List<ComponentName> = listOf(
            ComponentName(context, MobWidgetProvider.Small::class.java),
            ComponentName(context, MobWidgetProvider.Medium::class.java),
            ComponentName(context, MobWidgetProvider.Large::class.java),
        )

        fun allWidgetIds(context: Context): IntArray {
            val manager = AppWidgetManager.getInstance(context)
            return componentNames(context)
                .flatMap { manager.getAppWidgetIds(it).toList() }
                .distinct()
                .toIntArray()
        }
    }
}





