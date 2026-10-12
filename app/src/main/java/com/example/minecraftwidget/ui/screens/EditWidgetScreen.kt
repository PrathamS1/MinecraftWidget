package com.example.minecraftwidget.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.minecraftwidget.catalog.MinecraftEntity
import com.example.minecraftwidget.data.BackgroundShape
import com.example.minecraftwidget.data.TapAction
import com.example.minecraftwidget.data.WidgetConfig
import com.example.minecraftwidget.sprite.IdleAnimator
import com.example.minecraftwidget.ui.PlacedWidget
import com.example.minecraftwidget.ui.SetPreview
import com.example.minecraftwidget.ui.components.*
import com.example.minecraftwidget.ui.theme.*

/**
 * Screen allowing the user to edit a widget already placed on their home screen.
 * Displays "Edit <EntityName> Widget" with existing configurations pre-filled,
 * and an "Apply changes" button that immediately updates the live widget.
 */
@Composable
fun EditWidgetScreen(
    placedWidget: PlacedWidget,
    sets: List<SetPreview>,
    onBack: () -> Unit,
    onApplyChanges: (WidgetConfig) -> Unit,
    onPlayCry: (Int) -> Unit,
) {
    BackHandler(onBack = onBack)

    val entry = placedWidget.entry ?: return
    val initialConfig = placedWidget.config

    var chosenSetId by remember(placedWidget.widgetId) { mutableStateOf<String?>(initialConfig.setId) }
    val selected = chosenSetId
        ?: sets.find { it.set.id == initialConfig.setId }?.set?.id
        ?: sets.firstOrNull { it.set.animated }?.set?.id
        ?: sets.firstOrNull()?.set?.id

    var idleStyle by remember(placedWidget.widgetId) { mutableStateOf(initialConfig.idleStyle) }
    var showBackground by remember(placedWidget.widgetId) { mutableStateOf(initialConfig.showBackground) }
    var backgroundShape by remember(placedWidget.widgetId) { mutableStateOf(initialConfig.backgroundShape) }
    var backgroundColor by remember(placedWidget.widgetId) { mutableStateOf(initialConfig.backgroundColor) }
    var cornerRadiusDp by remember(placedWidget.widgetId) { mutableStateOf(initialConfig.cornerRadiusDp) }
    var tapAction by remember(placedWidget.widgetId) { mutableStateOf(initialConfig.tapAction) }

    val gridState = rememberLazyGridState()

    Column(
        Modifier
            .fillMaxSize()
            .dottedPaper()
    ) {
        // Top Header
        Row(
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MinecraftIconButton(Icons.AutoMirrored.Filled.ArrowBack, "Back", onBack)

            Text(
                text = "Edit ${entry.name} Widget",
                style = MaterialTheme.typography.headlineMedium,
                color = Obsidian,
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Balance placeholder
            Spacer(Modifier.size(48.dp))
        }

        // Hero Sprite in the Center with ambient glow
        val hero = sets.find { it.set.id == selected } ?: sets.firstOrNull()
        val entityColor = entityAmbienceColor(entry)
        val ambientColor = if (showBackground) {
            androidx.compose.ui.graphics.Color(backgroundColor).copy(alpha = 0.28f)
        } else {
            entityColor.copy(alpha = 0.28f)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .background(
                    androidx.compose.ui.graphics.Brush.radialGradient(
                        colors = listOf(ambientColor, androidx.compose.ui.graphics.Color.Transparent),
                        radius = 450f
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (hero != null) {
                var hops by remember(entry.id) { mutableStateOf(0) }
                val interactions = remember { MutableInteractionSource() }

                // Shaped background directly behind entity
                if (showBackground) {
                    val shape = when (backgroundShape) {
                        BackgroundShape.SQUARE -> androidx.compose.foundation.shape.RoundedCornerShape(0.dp)
                        BackgroundShape.ROUNDED -> androidx.compose.foundation.shape.RoundedCornerShape(cornerRadiusDp.coerceAtLeast(16).dp)
                        BackgroundShape.CIRCLE -> androidx.compose.foundation.shape.CircleShape
                        BackgroundShape.BLOB -> BlobShape
                    }
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(shape)
                            .background(androidx.compose.ui.graphics.Color(backgroundColor))
                    )
                }

                SpriteImage(
                    hero.url,
                    entry.displayName,
                    Modifier
                        .size(160.dp)
                        .pressScale(interactions)
                        .tapWobble(hops)
                        .clickable(interactionSource = interactions, indication = null) {
                            hops++
                            onPlayCry(entry.id)
                        },
                    spriteSet = hero.set
                )
            } else {
                EntityIcon(entry.id, entry.displayName, size = 160.dp)
            }
        }

        // Variant Grid & Config Below
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(StoneChalk)
                .topRule(width = 4.dp, color = StoneDark)
        ) {
            SpriteSetGrid(
                sets = sets,
                selectedSetId = selected,
                onSelect = { chosenSetId = it },
                modifier = Modifier.weight(1f),
                state = gridState,
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 16.dp),
                loading = sets.isEmpty(),
                header = {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Column {
                            SectionHeader("Variants", Modifier.padding(bottom = 8.dp))
                        }
                    }
                },
                footer = {
                    if (selected != null) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            val isAnimated = sets.find { it.set.id == selected }?.set?.animated == true
                            WidgetConfigSection(
                                isAnimated = isAnimated,
                                idleStyle = idleStyle,
                                onIdleStyleChange = { idleStyle = it },
                                showBackground = showBackground,
                                onShowBackgroundChange = { showBackground = it },
                                backgroundShape = backgroundShape,
                                onBackgroundShapeChange = { backgroundShape = it },
                                backgroundColor = backgroundColor,
                                onBackgroundColorChange = { backgroundColor = it },
                                cornerRadiusDp = cornerRadiusDp,
                                onCornerRadiusDpChange = { cornerRadiusDp = it },
                                tapAction = tapAction,
                                onTapActionChange = { tapAction = it },
                                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                            )
                        }
                    }
                }
            )

            // Apply Changes Button Footer
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(StoneLight)
                    .topRule()
                    .navigationBarsPadding()
                    .padding(16.dp),
            ) {
                MinecraftButton(
                    text = "Apply changes",
                    onClick = {
                        selected?.let { setId ->
                            val updatedConfig = initialConfig.copy(
                                setId = setId,
                                idleStyle = idleStyle,
                                showBackground = showBackground,
                                backgroundShape = backgroundShape,
                                backgroundColor = backgroundColor,
                                cornerRadiusDp = cornerRadiusDp,
                                tapAction = tapAction,
                            )
                            onApplyChanges(updatedConfig)
                        }
                    },
                    icon = Icons.Default.Check,
                    enabled = selected != null,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
