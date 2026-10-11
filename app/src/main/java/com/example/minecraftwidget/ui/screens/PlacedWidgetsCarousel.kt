package com.example.minecraftwidget.ui.screens

import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.blur
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.example.minecraftwidget.ui.PlacedWidget
import com.example.minecraftwidget.ui.components.EntityIcon
import com.example.minecraftwidget.ui.components.SectionHeader
import com.example.minecraftwidget.ui.components.pressScale
import com.example.minecraftwidget.ui.theme.CardColor
import kotlin.math.absoluteValue

import androidx.compose.ui.unit.lerp
import com.example.minecraftwidget.ui.theme.sticker
import com.example.minecraftwidget.ui.theme.StoneDark
import com.example.minecraftwidget.ui.theme.Obsidian
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun PlacedWidgetsCarousel(placed: List<PlacedWidget>, onEdit: (Int) -> Unit) {
    if (placed.isEmpty()) return

    Column {
        SectionHeader("Added To Home", Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(8.dp))
        
        val listState = rememberLazyListState()
        val itemSize = 160.dp
        val itemSpacing = (-20).dp
        
        val screenWidth = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp.dp
        val sidePadding = (screenWidth - itemSize) / 2
        
        LazyRow(
            state = listState,
            contentPadding = if (placed.size == 1) PaddingValues(horizontal = 0.dp) else PaddingValues(horizontal = sidePadding),
            horizontalArrangement = if (placed.size == 1) Arrangement.Center else Arrangement.spacedBy(itemSpacing),
            modifier = Modifier.fillMaxWidth().height(220.dp),
            verticalAlignment = Alignment.CenterVertically,
            flingBehavior = androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior(listState)
        ) {
            itemsIndexed(placed, key = { _, it -> it.widgetId }) { index, widget ->
                val itemInfo = listState.layoutInfo.visibleItemsInfo.find { it.index == index }
                val signedOffset = if (itemInfo != null) {
                    val centerOfViewport = listState.layoutInfo.viewportStartOffset + listState.layoutInfo.viewportEndOffset / 2f
                    val centerOfItem = itemInfo.offset + itemInfo.size / 2f
                    (centerOfItem - centerOfViewport) / (itemInfo.size.toFloat().coerceAtLeast(1f))
                } else {
                    0f
                }
                val absoluteOffset = signedOffset.absoluteValue
                
                val scale = (1f - (absoluteOffset * 0.2f)).coerceIn(0.6f, 1f)
                val alpha = (1f - (absoluteOffset * 0.4f)).coerceIn(0.4f, 1f)
                val blurRadiusDp = (absoluteOffset * 4f).coerceIn(0f, 8f).dp

                val interactions = remember { MutableInteractionSource() }
                
                Box(
                    modifier = Modifier
                        .zIndex(100f - absoluteOffset)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            this.alpha = alpha
                        }
                        .blur(blurRadiusDp)
                        .size(itemSize)
                        .pressScale(interactions)
                        .sticker(
                            shape = RoundedCornerShape(2.dp),
                            fill = CardColor,
                            borderWidth = 2.dp,
                            outline = Obsidian,
                            lift = 3.dp,
                        )
                        .clickable(
                            interactionSource = interactions,
                            indication = null,
                        ) { onEdit(widget.widgetId) }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EntityIcon(widget.config.entityId, null, size = itemSize)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}
