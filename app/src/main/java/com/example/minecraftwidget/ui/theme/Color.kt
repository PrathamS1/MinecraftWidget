package com.example.minecraftwidget.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * The app's palette: Minecraft-inspired colors to replace the old retro theme.
 *
 * Uses stone grays, dirt browns, and prominent colors like Creeper Green and Diamond Blue.
 */

/** General background color - a light stone grey. */
val StoneLight = Color(0xFFF0F0F0)

/** A darker stone/dirt accent for borders and subtle elements. */
val StoneDark = Color(0xFF9E9E9E)

/** Card backgrounds, usually pure white or very light gray. */
val CardColor = Color(0xFFFFFFFF)

/** Every outline, every piece of body copy. Near-black (Obsidian). */
val Obsidian = Color(0xFF1E1E1E)

/** Secondary copy. Lighter gray. */
val ObsidianSoft = Color(0xFF757575)

/** Primary accent color - Creeper Green. */
val CreeperGreen = Color(0xFF0DA928)

/** Secondary accent color - Grass Green. */
val GrassGreen = Color(0xFF5B9D42)

/** Highlight/action color - Diamond Blue. */
val DiamondBlue = Color(0xFF53F0DC)

/** Background for sprites (like the sky or water). */
val WaterBlue = Color(0xFF8CBED6)

/** Disabled or subtle background fills. */
val StoneChalk = Color(0xFFE0E0E0)

/**
 * Minecraft category colors used for chips. Keyed by the lowercase category/type name.
 */
val TypeColors: Map<String, Color> = mapOf(
    "hostile" to Color(0xFFD32F2F),
    "neutral" to Color(0xFFFBC02D),
    "friendly" to Color(0xFF388E3C),
    "blocks" to Color(0xFF795548),
    "items" to Color(0xFF00BCD4),
    "trainer" to Color(0xFFFF9800),
    "natural" to Color(0xFF4CAF50),
    "weapon" to Color(0xFF607D8B),
    "default" to Color(0xFF9E9E9E),
    "unknown" to Color(0xFF68A090)
)

fun typeColor(type: String): Color = TypeColors[type.lowercase()] ?: TypeColors.getValue("unknown")

/**
 * A readable Obsidian colour to put on top of [background].
 */
fun onColorFor(background: Color): Color {
    val luminance = 0.2126f * channel(background.red) +
        0.7152f * channel(background.green) +
        0.0722f * channel(background.blue)
    return if (luminance > 0.45f) Obsidian else Color.White
}

private fun channel(value: Float): Float =
    if (value <= 0.03928f) value / 12.92f else Math.pow(((value + 0.055f) / 1.055f).toDouble(), 2.4).toFloat()


