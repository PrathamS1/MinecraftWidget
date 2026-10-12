package com.example.minecraftwidget.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.minecraftwidget.ui.PlacedWidget
import com.example.minecraftwidget.ui.components.EntityIcon
import com.example.minecraftwidget.ui.components.SectionHeader
import com.example.minecraftwidget.ui.components.pressScale
import com.example.minecraftwidget.ui.theme.CardColor
import com.example.minecraftwidget.ui.theme.Obsidian
import com.example.minecraftwidget.ui.theme.sticker

@Composable
fun PlacedWidgetsCarousel(placed: List<PlacedWidget>, onEdit: (Int) -> Unit) {
    if (placed.isEmpty()) return

    Column {
        SectionHeader("Added To Home", Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(8.dp))

        val itemSize = 140.dp

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items(placed, key = { it.widgetId }) { widget ->
                val interactions = remember { MutableInteractionSource() }

                Box(
                    modifier = Modifier
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
                    contentAlignment = Alignment.Center,
                ) {
                    EntityIcon(widget.config.entityId, null, size = itemSize)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}
