package com.example.minecraftwidget.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.minecraftwidget.ui.SetPreview
import com.example.minecraftwidget.ui.components.SpriteImage
import com.example.minecraftwidget.ui.theme.sticker
import com.example.minecraftwidget.ui.theme.StoneChalk
import com.example.minecraftwidget.ui.theme.StoneDark
import com.example.minecraftwidget.ui.theme.CreeperGreen
import com.example.minecraftwidget.ui.theme.Obsidian
import com.example.minecraftwidget.ui.theme.CardColor

import com.example.minecraftwidget.ui.components.Caption

@Composable
fun SpriteSetGrid(
    sets: List<SetPreview>,
    selectedSetId: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    state: LazyGridState = rememberLazyGridState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    loading: Boolean = false,
    header: (LazyGridScope.() -> Unit)? = null,
    footer: (LazyGridScope.() -> Unit)? = null,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 80.dp),
        state = state,
        modifier = modifier,
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (header != null) header()

        if (loading) {
            items(12) {
                Box(
                    Modifier.aspectRatio(1f).sticker(shape = MaterialTheme.shapes.small, fill = StoneChalk)
                )
            }
        } else {
            items(sets, key = { it.set.id }) { preview ->
                val selected = preview.set.id == selectedSetId
                Column(
                    Modifier
                        .sticker(
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp),
                            fill = if (selected) StoneDark else CardColor,
                            lift = if (selected) 4.dp else 2.dp,
                            borderWidth = if (selected) 3.dp else 2.dp,
                            outline = if (selected) CreeperGreen else Obsidian
                        )
                        .clickable { onSelect(preview.set.id) }
                        .padding(4.dp),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    SpriteImage(
                        url = preview.url,
                        contentDescription = preview.set.label,
                        modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                        spriteSet = preview.set
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(preview.set.label, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                }
            }
        }
        
        if (footer != null) footer()
    }
}

@Composable
fun SpriteSetGridCaption(count: Int, animated: Int) {
    Caption("$count variants ($animated animated)")
}
