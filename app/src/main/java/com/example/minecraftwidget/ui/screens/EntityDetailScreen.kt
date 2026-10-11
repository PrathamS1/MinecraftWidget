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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.minecraftwidget.catalog.MinecraftEntity
import com.example.minecraftwidget.ui.SetPreview
import com.example.minecraftwidget.ui.components.*
import com.example.minecraftwidget.ui.theme.*
import com.example.minecraftwidget.data.WidgetConfig
import com.example.minecraftwidget.data.BackgroundShape
import com.example.minecraftwidget.data.TapAction
import com.example.minecraftwidget.sprite.IdleStyle
import com.example.minecraftwidget.sprite.IdleAnimator

@Composable
fun EntityDetailScreen(
    entry: MinecraftEntity,
    sets: List<SetPreview>,
    canPin: Boolean,
    onBack: () -> Unit,
    onPin: (WidgetConfig) -> Unit,
    onPlayCry: (Int) -> Unit,
) {
    BackHandler(onBack = onBack)

    var chosenSetId by remember(entry.id) { mutableStateOf<String?>(null) }
    val selected = chosenSetId
        ?: sets.firstOrNull { it.set.animated }?.set?.id
        ?: sets.firstOrNull()?.set?.id

    var idleStyle by remember(entry.id) { mutableStateOf(IdleAnimator.DEFAULT) }
    var showBackground by remember(entry.id) { mutableStateOf(false) }
    var backgroundShape by remember(entry.id) { mutableStateOf(BackgroundShape.ROUNDED) }
    var backgroundColor by remember(entry.id) { mutableStateOf(0xCC1B1F27.toInt()) }
    var cornerRadiusDp by remember(entry.id) { mutableStateOf(20) }
    var tapAction by remember(entry.id) { mutableStateOf(TapAction.WOBBLE) }
    
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
                text = entry.name,
                style = MaterialTheme.typography.headlineMedium,
                color = Obsidian,
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            // Empty placeholder for balance
            Spacer(Modifier.size(48.dp))
        }

        // Hero Sprite in the Center with upper ambient glow matched to entity
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

            // Pin Button Footer
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(StoneLight)
                    .topRule()
                    .navigationBarsPadding()
                    .padding(16.dp),
            ) {
                if (canPin) {
                    MinecraftButton(
                        text = "Add to home screen",
                        onClick = {
                            selected?.let { setId ->
                                val config = WidgetConfig(
                                    entityId = entry.id,
                                    setId = setId,
                                    shiny = false,
                                    idleStyle = idleStyle,
                                    showBackground = showBackground,
                                    backgroundShape = backgroundShape,
                                    backgroundColor = backgroundColor,
                                    cornerRadiusDp = cornerRadiusDp,
                                    tapAction = tapAction,
                                    cryEnabled = true
                                )
                                onPin(config)
                            }
                        },
                        icon = Icons.Default.Add,
                        enabled = selected != null,
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    Caption(
                        "This launcher does not support adding widgets from inside an app. " +
                            "Long-press your home screen and pick MinecraftWidget from the widget list.",
                    )
                }
            }
        }
    }
}

/**
 * Returns an iconic ambient glow color matching the specific Minecraft entity.
 */
fun entityAmbienceColor(entity: MinecraftEntity): androidx.compose.ui.graphics.Color {
    val key = "${entity.slug} ${entity.name}".lowercase()
    return when {
        "creeper" in key -> androidx.compose.ui.graphics.Color(0xFF48B53E)
        "enderman" in key || "ender_dragon" in key || "shulker" in key -> androidx.compose.ui.graphics.Color(0xFF9C27B0)
        "warden" in key || "sculk" in key -> androidx.compose.ui.graphics.Color(0xFF00ADB5)
        "fox" in key -> androidx.compose.ui.graphics.Color(0xFFE65100)
        "wolf" in key -> androidx.compose.ui.graphics.Color(0xFF90A4AE)
        "polar" in key || "snow" in key -> androidx.compose.ui.graphics.Color(0xFFB0BEC5)
        "drowned" in key -> androidx.compose.ui.graphics.Color(0xFF00897B)
        "husk" in key -> androidx.compose.ui.graphics.Color(0xFFD7CCC8)
        "zombie" in key -> androidx.compose.ui.graphics.Color(0xFF388E3C)
        "skeleton" in key || "stray" in key -> androidx.compose.ui.graphics.Color(0xFFCFD8DC)
        "spider" in key -> androidx.compose.ui.graphics.Color(0xFF8D6E63)
        "blaze" in key -> androidx.compose.ui.graphics.Color(0xFFFF9800)
        "ghast" in key -> androidx.compose.ui.graphics.Color(0xFFECEFF1)
        "piglin" in key || "hoglin" in key || "pig" in key -> androidx.compose.ui.graphics.Color(0xFFF48FB1)
        "mooshroom" in key -> androidx.compose.ui.graphics.Color(0xFFE53935)
        "cow" in key -> androidx.compose.ui.graphics.Color(0xFF795548)
        "sheep" in key -> androidx.compose.ui.graphics.Color(0xFFF5F5F5)
        "chicken" in key -> androidx.compose.ui.graphics.Color(0xFFFFEE58)
        "iron_golem" in key || "golem" in key -> androidx.compose.ui.graphics.Color(0xFF78909C)
        "illager" in key || "pillager" in key || "evoker" in key || "vindicator" in key || "illusioner" in key -> androidx.compose.ui.graphics.Color(0xFF546E7A)
        "villager" in key -> androidx.compose.ui.graphics.Color(0xFF8D6E63)
        "slime" in key -> androidx.compose.ui.graphics.Color(0xFF7CB342)
        "magma" in key -> androidx.compose.ui.graphics.Color(0xFFE64A19)
        "squid" in key -> androidx.compose.ui.graphics.Color(0xFF00E5FF)
        "bee" in key -> androidx.compose.ui.graphics.Color(0xFFFFC107)
        "axolotl" in key -> androidx.compose.ui.graphics.Color(0xFFF06292)
        "witch" in key -> androidx.compose.ui.graphics.Color(0xFF7B1FA2)
        "wither" in key -> androidx.compose.ui.graphics.Color(0xFF37474F)
        "allay" in key -> androidx.compose.ui.graphics.Color(0xFF29B6F6)
        "breeze" in key -> androidx.compose.ui.graphics.Color(0xFF7E57C2)
        "armadillo" in key -> androidx.compose.ui.graphics.Color(0xFF8D6E63)
        "bogged" in key -> androidx.compose.ui.graphics.Color(0xFF689F38)
        else -> {
            val palette = listOf(
                androidx.compose.ui.graphics.Color(0xFF4CAF50),
                androidx.compose.ui.graphics.Color(0xFF2196F3),
                androidx.compose.ui.graphics.Color(0xFFFF9800),
                androidx.compose.ui.graphics.Color(0xFF9C27B0),
                androidx.compose.ui.graphics.Color(0xFF00BCD4),
                androidx.compose.ui.graphics.Color(0xFFE91E63),
                androidx.compose.ui.graphics.Color(0xFF795548),
            )
            palette[kotlin.math.abs(entity.id) % palette.size]
        }
    }
}

