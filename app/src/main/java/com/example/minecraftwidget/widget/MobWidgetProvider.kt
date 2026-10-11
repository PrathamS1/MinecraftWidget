package com.example.minecraftwidget.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.example.minecraftwidget.catalog.CatalogRepository
import com.example.minecraftwidget.data.SpriteSource
import com.example.minecraftwidget.data.TapAction
import com.example.minecraftwidget.data.WidgetConfigStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Three concrete providers so the launcher's widget picker offers size presets. They
 * share every behaviour; only the `appwidget-provider` XML differs, and all three are
 * freely resizable afterwards.
 */
sealed class MobWidgetProvider : AppWidgetProvider() {

    class Small : MobWidgetProvider()
    class Medium : MobWidgetProvider()
    class Large : MobWidgetProvider()

    override fun onUpdate(context: Context, manager: AppWidgetManager, widgetIds: IntArray) {
        // goAsync() is safe here: AppWidgetProvider dispatches onUpdate synchronously
        // from onReceive, so we are still inside the receiver's window.
        WidgetUpdater.request(context, widgetIds, goAsync())
    }

    /** Fired when the user resizes the widget — the sprite is re-planned for the new box. */
    override fun onAppWidgetOptionsChanged(
        context: Context,
        manager: AppWidgetManager,
        widgetId: Int,
        newOptions: Bundle?,
    ) {
        WidgetUpdater.request(context, intArrayOf(widgetId), goAsync())
    }

    override fun onDeleted(context: Context, widgetIds: IntArray) {
        val store = WidgetConfigStore(context)
        scope.launch { store.remove(widgetIds) }
    }

    /** Widget ids are reassigned after a backup restore; carry the settings across. */
    override fun onRestored(context: Context, oldWidgetIds: IntArray, newWidgetIds: IntArray) {
        val store = WidgetConfigStore(context)
        scope.launch {
            store.remap(oldWidgetIds, newWidgetIds)
            WidgetUpdater.request(context, newWidgetIds)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == WidgetActions.ACTION_TAP) {
            handleTap(context, intent)
            return
        }
        if (intent.action == WidgetActions.ACTION_PINNED) {
            adoptPinned(context, goAsync())
            return
        }
        // Everything else goes through onUpdate, which renders via WidgetRenderer.
        super.onReceive(context, intent)
    }

    /**
     * Renders every placed widget that has no settings yet.
     *
     * This is the first render for a widget added by the app's "Add to home screen"
     * button. Because these providers declare a configuration activity, the system sends
     * no APPWIDGET_UPDATE for a freshly placed widget, and `requestPinAppWidget` does not
     * launch that activity — so nothing else in the app would ever draw it, and the
     * pending pin choice would never be consumed.
     *
     * The callback carries no widget id, so the new widget is found by elimination: it is
     * the placed one the config store has never heard of. Rendering it runs
     * [WidgetRenderer.resolveConfig], which adopts the Entity the user picked.
     */
    private fun adoptPinned(context: Context, pending: BroadcastReceiver.PendingResult) {
        val appContext = context.applicationContext
        scope.launch {
            try {
                val store = WidgetConfigStore(appContext)
                val renderer = WidgetRenderer(appContext)
                for (id in WidgetRenderer.allWidgetIds(appContext)) {
                    if (store.exists(id)) continue
                    runCatching { renderer.render(id) }
                        .onFailure { Log.e(TAG, "could not render pinned widget $id", it) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "pin callback failed", e)
            } finally {
                pending.finish()
            }
        }
    }

    /**
     * Widgets get a click callback and nothing else — no touch-down, no gestures — so
     * every interaction is funnelled through this one broadcast.
     *
     * `goAsync()` holds the receiver alive for the work, and Android grants it roughly ten
     * seconds before declaring the app unresponsive. **Nothing on this path is allowed to
     * outlive that**, which is what [TAP_BUDGET_MS] enforces and what `pending.finish()` in
     * the `finally` guarantees. Both halves of the work here can be slow on a cold cache —
     * the cry fetch and the "this widget is showing an error, re-render it" branch each go
     * to the network — and before the budget existed a tap on a widget with no cached art
     * could sit in `SpriteSource` for the better part of a minute and produce an ANR.
     */
    private fun handleTap(context: Context, intent: Intent) {
        val widgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID,
        )
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return

        val appContext = context.applicationContext
        val payload = TapPayload.read(
            getInt = { key -> intent.getIntExtra(key, 0).takeIf { intent.hasExtra(key) } },
            getString = { key -> intent.getStringExtra(key) },
        )
        // Sound first, before a coroutine, a DataStore read or anything else is spun up.
        val cry = payload?.takeIf { it.cries }
            ?.let { CryPlayer.play(appContext, it.entityId, it.legacyCry) }

        val pending = goAsync()
        scope.launch {
            try {
                withTimeoutOrNull(TAP_BUDGET_MS) { runTap(appContext, widgetId, payload, cry) }
                    ?: Log.w(TAG, "tap on widget $widgetId ran past its ${TAP_BUDGET_MS}ms budget")
            } catch (e: Exception) {
                Log.e(TAG, "tap handling failed for widget $widgetId", e)
            } finally {
                pending.finish()
            }
        }
    }

    /**
     * The tap itself, minus the receiver bookkeeping. Split out so the timeout in
     * [handleTap] wraps the work and not the `finish()` that must happen regardless.
     *
     * **Nothing here waits for a cry to end.** The receiver used to `join()` the whole cry,
     * and broadcasts to one app are delivered one after another — so a second tap sat in the
     * queue until the first cry had finished, then played late. Now the receiver is held
     * only until sound has *started* (which keeps the process out of the cached state for
     * the moment that matters), and everything else runs alongside the cry, never ahead of it.
     *
     * @param cry the cry [handleTap] already started from the intent's payload, if any.
     */
    private suspend fun runTap(
        appContext: Context,
        widgetId: Int,
        payload: TapPayload?,
        cry: CryHandle?,
    ) = coroutineScope {
        val store = WidgetConfigStore(appContext)
        val config = store.get(widgetId)

        // A widget drawn by 1.4 has no payload; its first tap decides from the store instead.
        val sound = cry ?: if (payload == null && TapPayload.of(config).cries) {
            CryPlayer.play(appContext, config.entityId, config.legacyCry)
        } else {
            null
        }

        // A widget with no cached art is not showing a Pokémon — it is showing an error,
        // and the only thing its owner can mean by tapping it is "try again". Honour that
        // alongside the configured action, whatever that action is: the default is CRY, which
        // never re-renders, so without this a widget that failed once stayed broken until a
        // reboot. SpriteSource remembers confirmed 404s, so a sprite that genuinely does not
        // exist costs no round trip here.
        val retryRender = !SpriteSource(appContext).isSpriteCached(config.spriteKey)
        if (retryRender) launch { WidgetRenderer(appContext).render(widgetId) }

            when (config.tapAction) {
                TapAction.PLAY_SOUND -> Unit // Already playing via cry sound

                TapAction.PLAY_ANIMATION -> {
                    val catalog = CatalogRepository.get(appContext)
                    val set = catalog.set(config.setId)
                    val animDuration = if (set != null) {
                        if (set.frameDelaysMs.isNotEmpty()) {
                            set.frameDelaysMs.sum().toLong()
                        } else if (set.frameCount != null && set.frameCount > 1) {
                            (set.frameCount * 100L)
                        } else {
                            1000L
                        }
                    } else {
                        1000L
                    }
                    val until = System.currentTimeMillis() + animDuration
                    store.update(widgetId) { it.copy(excitedUntilMs = until) }
                    if (!retryRender) WidgetRenderer(appContext).render(widgetId)
                    WidgetUpdater.requestDelayed(appContext, widgetId, animDuration)
                }

                TapAction.WOBBLE -> {
                    val wobbleDuration = 1040L // 8 frames * 130ms = 1 single wobble cycle
                    val until = System.currentTimeMillis() + wobbleDuration
                    store.update(widgetId) { it.copy(excitedUntilMs = until) }
                    if (!retryRender) WidgetRenderer(appContext).render(widgetId)
                    WidgetUpdater.requestDelayed(appContext, widgetId, wobbleDuration)
                }

                TapAction.NONE -> Unit
            }



        sound?.let { withTimeoutOrNull(CRY_START_WAIT_MS) { it.started.await() } }
        Unit
    }

    companion object {
        private const val TAG = "MobWidgetProvider"
        const val EXCITED_DURATION_MS = 2_200L

        /**
         * The hard ceiling on one tap. Android grants a broadcast receiver roughly ten
         * seconds before it is declared unresponsive; this leaves margin inside that for
         * the `finish()` and for a slow device.
         */
        private const val TAP_BUDGET_MS = 8_000L

        /**
         * How long a tap holds the receiver for a cry to *begin*. A cry that is cached but
         * not yet decoded starts well inside this; one that has to download may not, and
         * then the download carries on without the receiver.
         */
        private const val CRY_START_WAIT_MS = 1_500L

        /**
         * Broadcast receivers are torn down as soon as they return, so the work has to
         * outlive the instance. `goAsync()` keeps the *process* alive; this scope keeps
         * the coroutine off the main thread while it does.
         */
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }
}



