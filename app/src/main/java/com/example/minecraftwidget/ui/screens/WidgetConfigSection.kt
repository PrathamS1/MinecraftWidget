package com.example.minecraftwidget.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.minecraftwidget.data.BackgroundShape
import com.example.minecraftwidget.data.TapAction
import com.example.minecraftwidget.sprite.IdleAnimator
import com.example.minecraftwidget.sprite.IdleStyle
import com.example.minecraftwidget.ui.components.MinecraftChip
import com.example.minecraftwidget.ui.components.SectionHeader
import com.example.minecraftwidget.ui.theme.BlobShape
import com.example.minecraftwidget.ui.theme.CreeperGreen
import com.example.minecraftwidget.ui.theme.Obsidian
import com.example.minecraftwidget.ui.theme.sticker

@Composable
fun WidgetConfigSection(
    isAnimated: Boolean,
    idleStyle: IdleStyle,
    onIdleStyleChange: (IdleStyle) -> Unit,
    showBackground: Boolean,
    onShowBackgroundChange: (Boolean) -> Unit,
    backgroundShape: BackgroundShape = BackgroundShape.ROUNDED,
    onBackgroundShapeChange: (BackgroundShape) -> Unit = {},
    backgroundColor: Int,
    onBackgroundColorChange: (Int) -> Unit,
    cornerRadiusDp: Int,
    onCornerRadiusDpChange: (Int) -> Unit,
    tapAction: TapAction,
    onTapActionChange: (TapAction) -> Unit,
    modifier: Modifier = Modifier
) {
    var backgroundType by remember(showBackground) {
        mutableStateOf(if (showBackground) "Color" else "None")
    }

    Column(modifier = modifier.padding(vertical = 16.dp)) {
        // Animation Section: Animated, Static, Swaying
        SectionHeader("Animation", Modifier.padding(horizontal = 16.dp))
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MinecraftChip(
                label = "Animated",
                selected = idleStyle == IdleStyle.AUTO || idleStyle == IdleAnimator.DEFAULT,
                onClick = { onIdleStyleChange(IdleStyle.AUTO) }
            )
            MinecraftChip(
                label = "Static",
                selected = idleStyle == IdleStyle.NONE,
                onClick = { onIdleStyleChange(IdleStyle.NONE) }
            )
            MinecraftChip(
                label = "Swaying",
                selected = idleStyle == IdleStyle.SWAY,
                onClick = { onIdleStyleChange(IdleStyle.SWAY) }
            )
        }
        Spacer(Modifier.height(16.dp))

        // Background Section: None, Color, Scene
        SectionHeader("Background", Modifier.padding(horizontal = 16.dp))
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MinecraftChip(
                label = "None",
                selected = backgroundType == "None",
                onClick = {
                    backgroundType = "None"
                    onShowBackgroundChange(false)
                }
            )
            MinecraftChip(
                label = "Color",
                selected = backgroundType == "Color",
                onClick = {
                    backgroundType = "Color"
                    onShowBackgroundChange(true)
                }
            )
            MinecraftChip(
                label = "Scene",
                selected = backgroundType == "Scene",
                onClick = {
                    backgroundType = "Scene"
                    onShowBackgroundChange(true)
                }
            )
        }

        if (backgroundType == "Color" || backgroundType == "Scene") {
            // Shape selector: Square, Rounded, Circle, Blob
            Text(
                text = "Shape",
                style = MaterialTheme.typography.labelMedium,
                color = Obsidian,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                items(BackgroundShape.values()) { shape ->
                    MinecraftChip(
                        label = shape.label,
                        selected = backgroundShape == shape,
                        onClick = { onBackgroundShapeChange(shape) }
                    )
                }
            }

            if (backgroundType == "Color") {
                Text(
                    text = "Color",
                    style = MaterialTheme.typography.labelMedium,
                    color = Obsidian,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(vertical = 6.dp)
                ) {
                    val colors = listOf(
                        0xCC1B1F27.toInt(),
                        0xCC4CAF50.toInt(),
                        0xCC2196F3.toInt(),
                        0xCCF44336.toInt(),
                        0xCC9E9E9E.toInt(),
                        0xCCE91E63.toInt(),
                        0xFFFF9800.toInt(),
                        0xCC9C27B0.toInt(),
                        0xCC795548.toInt(),
                    )
                    items(colors) { color ->
                        val selected = backgroundColor == color
                        val shape = when (backgroundShape) {
                            BackgroundShape.SQUARE -> RoundedCornerShape(0.dp)
                            BackgroundShape.ROUNDED -> RoundedCornerShape(cornerRadiusDp.coerceAtLeast(10).dp)
                            BackgroundShape.CIRCLE -> CircleShape
                            BackgroundShape.BLOB -> BlobShape
                        }
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(shape)
                                .background(Color(color))
                                .clickable { onBackgroundColorChange(color) }
                                .sticker(
                                    shape = shape,
                                    fill = Color.Transparent,
                                    borderWidth = if (selected) 3.dp else 1.dp,
                                    outline = if (selected) CreeperGreen else Obsidian,
                                    lift = if (selected) 4.dp else 0.dp
                                )
                        )
                    }
                }
            } else {
                // Prepared scene choices (preparation for future scenes)
                Text(
                    text = "Scene (Coming soon)",
                    style = MaterialTheme.typography.labelMedium,
                    color = Obsidian,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                Row(
                    Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MinecraftChip(label = "Overworld", selected = true, onClick = {})
                    MinecraftChip(label = "Nether", selected = false, onClick = {})
                    MinecraftChip(label = "The End", selected = false, onClick = {})
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Tap Action Section: Wobble, Play Sound, Play Animation, Do Nothing
        SectionHeader("Tap Action", Modifier.padding(horizontal = 16.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            val actions = listOf(
                TapAction.WOBBLE to "Wobble",
                TapAction.PLAY_SOUND to "Play Sound",
                TapAction.PLAY_ANIMATION to "Play Animation",
                TapAction.NONE to "Do Nothing",
            )
            items(actions) { (action, label) ->
                MinecraftChip(
                    label = label,
                    selected = tapAction == action,
                    onClick = { onTapActionChange(action) }
                )
            }
        }
    }
}
