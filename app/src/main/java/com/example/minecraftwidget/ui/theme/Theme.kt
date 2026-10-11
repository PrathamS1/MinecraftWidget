package com.example.minecraftwidget.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * One scheme, always light.
 *
 * The app used to follow the system into a dark Material palette and, on Android 12+,
 * into the wallpaper's dynamic colours. Both are dropped deliberately: the StoneLight stock,
 * the Obsidian outline and the three accents *are* the design, and a themed variant of them
 * is just a worse version of the same screen. A widget host app is also seen in short
 * bursts next to a colourful home screen, where the bright stock reads as an object
 * rather than as a window.
 */
private val PokeScheme = lightColorScheme(
    primary = CreeperGreen,
    onPrimary = Color.White,
    primaryContainer = GrassGreen,
    onPrimaryContainer = Color.White,

    secondary = Obsidian,
    onSecondary = StoneLight,
    secondaryContainer = DiamondBlue,
    onSecondaryContainer = Obsidian,

    tertiary = WaterBlue,
    onTertiary = Obsidian,

    background = StoneLight,
    onBackground = Obsidian,
    surface = CardColor,
    onSurface = Obsidian,
    surfaceVariant = StoneChalk,
    onSurfaceVariant = ObsidianSoft,

    outline = Obsidian,
    outlineVariant = StoneDark,

    error = Color(0xFFB3261E),
    onError = Color.White,
)

/**
 * Chunky, and concentric by construction.
 *
 * Nested surfaces here are padded by 8.dp, so each step down the scale is 8.dp smaller
 * than the one above it: a [Shapes.large] CardColor holding a [Shapes.medium] panel holding a
 * [Shapes.small] chip all end up with visually parallel corners instead of the pinched
 * look you get from repeating one radius.
 */
val PokeShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp),
)

@Composable
fun MinecraftWidgetTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PokeScheme,
        typography = PokeTypography,
        shapes = PokeShapes,
        content = content,
    )
}




