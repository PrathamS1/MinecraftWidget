package com.example.minecraftwidget.data

import com.example.minecraftwidget.catalog.SpriteKey
import com.example.minecraftwidget.sprite.IdleAnimator
import com.example.minecraftwidget.sprite.IdleStyle

/** What background shape is drawn behind the entity. */
enum class BackgroundShape(val label: String) {
    SQUARE("Square"),
    ROUNDED("Rounded"),
    CIRCLE("Circle"),
    BLOB("Blob"),
}

/** What a tap on the widget does. */
enum class TapAction(val label: String, val description: String) {
    WOBBLE("Wobble", "Cute bouncy wobble animation"),
    PLAY_SOUND("Play Sound", "Plays random entity sound effect"),
    PLAY_ANIMATION("Play Animation", "Plays the entity's default animation"),
    NONE("Do nothing", "Ignores taps");

    companion object {
        val CRY = PLAY_SOUND
        val EXCITE = WOBBLE
    }
}

/**
 * Animation smoothness. Higher frame rates cost bitmap memory, and the widget memory
 * ceiling is fixed by screen size — so on a large widget with a big sprite the planner
 * may quietly deliver less than requested. It never crashes to get there.
 */
enum class Smoothness(val label: String, val fps: Int) {
    SMOOTH("Smooth", 14),
    BALANCED("Balanced", 10),
    LIGHT("Light", 7),
}

/**
 * How large the sprite is drawn inside whatever space the launcher gives the widget.
 *
 * These are what the sprite *looks* like on screen, and 1.5 honours them exactly: 4× means
 * each source pixel covers four screen pixels. They used to be caps on a bitmap upscale that
 * the memory budget quietly overrode, which is why "Fill" and "4×" often did nothing.
 * Names are stored, so they must not change.
 */
enum class Fill(val label: String, val description: String, val multiple: Int?) {
    FIT("Fill the widget", "As big as the widget allows", null),
    TRUE_SIZE("True size", "Big Pokémon look big and small ones look small", null),
    X4("4×", "Four screen pixels per sprite pixel", 4),
    X3("3×", "Three screen pixels per sprite pixel", 3),
    X2("2×", "Two screen pixels per sprite pixel", 2),
    X1("Original size", "One screen pixel per sprite pixel", 1),
}

data class WidgetConfig(
    val entityId: Int = 1,
    val setId: String = "mobs_allay",
    val shiny: Boolean = false,
    val back: Boolean = false,
    val female: Boolean = false,
    val style: String? = null,

    /**
     * Mirror the sprite left to right, so it can face the other way on the home screen.
     */
    val flipHorizontal: Boolean = false,

    val showBackground: Boolean = false,
    val backgroundShape: BackgroundShape = BackgroundShape.ROUNDED,
    val backgroundColor: Int = 0xCC1B1F27.toInt(),
    val cornerRadiusDp: Int = 20,

    val backgroundId: String? = null,

    val cryEnabled: Boolean = false,
    val legacyCry: Boolean = false,

    val smoothness: Smoothness = Smoothness.BALANCED,
    val fill: Fill = Fill.FIT,
    val tapAction: TapAction = TapAction.PLAY_ANIMATION,

    val idleStyle: IdleStyle = IdleAnimator.DEFAULT,

    /** Set while an animated tap burst is running. */
    val excitedUntilMs: Long = 0L,
) {
    val spriteKey: SpriteKey
        get() = SpriteKey(setId, entityId, back = back, shiny = shiny, female = female, style = style)
}



