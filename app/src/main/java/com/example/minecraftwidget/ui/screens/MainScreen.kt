package com.example.minecraftwidget.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.minecraftwidget.catalog.MinecraftEntity
import com.example.minecraftwidget.data.WidgetConfig
import com.example.minecraftwidget.ui.MainUiState
import com.example.minecraftwidget.ui.PlacedWidget
import com.example.minecraftwidget.ui.components.Caption
import com.example.minecraftwidget.ui.components.EmptyHint
import com.example.minecraftwidget.ui.components.MinecraftButton
import com.example.minecraftwidget.ui.components.MinecraftChip
import com.example.minecraftwidget.ui.components.MinecraftIconButton
import com.example.minecraftwidget.ui.components.MinecraftSearchField
import com.example.minecraftwidget.ui.components.EntityIcon
import com.example.minecraftwidget.ui.components.SectionHeader
import com.example.minecraftwidget.ui.components.SpriteStage
import com.example.minecraftwidget.ui.components.pressScale
import com.example.minecraftwidget.ui.theme.Obsidian
import com.example.minecraftwidget.ui.theme.ObsidianSoft
import com.example.minecraftwidget.ui.theme.DiamondBlue
import com.example.minecraftwidget.ui.theme.WaterBlue
import com.example.minecraftwidget.ui.theme.dottedPaper
import com.example.minecraftwidget.ui.theme.sticker
import com.example.minecraftwidget.ui.theme.CardColor as CardColor
import com.example.minecraftwidget.ui.components.SwayingGrassBackground
import androidx.compose.foundation.background

/**
 * The app's home: search every Minecraft mob, see what is already on the home screen, open one
 * to choose its art.
 *
 * Both of the screens that used to be modal bottom sheets — a Minecraft mob's sprite sets and
 * the settings — are now full pages. Sheets dismiss on a downward drag, which is the same
 * gesture as scrolling their contents, and both of these are things the user scrolls.
 */
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon

@Composable
fun MainScreen(
    state: MainUiState,
    onQuery: (String) -> Unit,
    onCategory: (String?) -> Unit,
    onAnimatedOnly: (Boolean) -> Unit,
    onOpenDetail: (MinecraftEntity) -> Unit,
    onCloseDetail: () -> Unit,
    onPin: (WidgetConfig) -> Unit,
    onEditWidget: (Int) -> Unit,
    onCloseEditWidget: () -> Unit = {},
    onUpdateWidget: (Int, WidgetConfig) -> Unit = { _, _ -> },
    onClearCache: () -> Unit,
    onClearCrash: () -> Unit,
    onPlayCry: (Int) -> Unit,
) {
    var currentTab by remember { mutableStateOf(0) } // 0 = Home, 1 = Settings
    val detail = state.detail
    val editingWidget = state.editingWidget

    when {
        editingWidget != null -> EditWidgetScreen(
            placedWidget = editingWidget,
            sets = state.editingSets,
            onBack = onCloseEditWidget,
            onApplyChanges = { updatedConfig -> onUpdateWidget(editingWidget.widgetId, updatedConfig) },
            onPlayCry = onPlayCry,
        )
        detail != null -> EntityDetailScreen(
            entry = detail,
            sets = state.detailSets,
            canPin = state.canPin,
            onBack = onCloseDetail,
            onPin = onPin,
            onPlayCry = onPlayCry,
        )
        else -> {
            Box(modifier = Modifier.fillMaxSize()) {
                // Content
                Box(modifier = Modifier.fillMaxSize().padding(bottom = 80.dp)) {
                    if (currentTab == 1) {
                        SettingsScreen(
                            cacheBytes = state.cacheBytes,
                            onClearCache = onClearCache,
                            onBack = { currentTab = 0 },
                            crashReport = state.crashReport,
                            onClearCrash = onClearCrash,
                            message = state.message,
                            placedCount = state.placed.size,
                        )
                    } else {
                        BrowseScreen(
                            state = state,
                            onQuery = onQuery,
                            onCategory = onCategory,
                            onAnimatedOnly = onAnimatedOnly,
                            onOpenDetail = onOpenDetail,
                            onEditWidget = onEditWidget,
                        )
                    }
                }

                // Grass Background + Hotbar Nav
                Box(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()) {
                    SwayingGrassBackground(modifier = Modifier.align(Alignment.BottomCenter))
                    MinecraftHotbarNav(
                        currentTab = currentTab,
                        onTabSelected = { currentTab = it },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }
    }
}

@Composable
fun MinecraftHotbarNav(
    currentTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(androidx.compose.ui.graphics.Color(0xCC181818), androidx.compose.foundation.shape.RoundedCornerShape(6.dp))
                .padding(4.dp)
        ) {
            val navItems = listOf(
                Pair(0, Icons.Default.Home) to "Home",
                Pair(1, Icons.Default.Settings) to "Settings"
            )
            for ((item, label) in navItems) {
                val (tabIndex, icon) = item
                val isSelected = currentTab == tabIndex

                Box(
                    Modifier
                        .size(54.dp)
                        .padding(2.dp)
                        .background(
                            if (isSelected) androidx.compose.ui.graphics.Color.White else androidx.compose.ui.graphics.Color(0xFF8B8B8B),
                            androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                        )
                        .clickable { onTabSelected(tabIndex) }
                        .padding(if (isSelected) 3.dp else 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                if (isSelected) androidx.compose.ui.graphics.Color(0xFF2E7D32) else androidx.compose.ui.graphics.Color(0xFF373737),
                                androidx.compose.foundation.shape.RoundedCornerShape(2.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            icon,
                            contentDescription = label,
                            tint = androidx.compose.ui.graphics.Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun BrowseScreen(
    state: MainUiState,
    onQuery: (String) -> Unit,
    onCategory: (String?) -> Unit,
    onAnimatedOnly: (Boolean) -> Unit,
    onOpenDetail: (MinecraftEntity) -> Unit,
    onEditWidget: (Int) -> Unit,
) {
    var headerHeightPx by remember { androidx.compose.runtime.mutableIntStateOf(0) }
    var searchHeightPx by remember { androidx.compose.runtime.mutableIntStateOf(0) }
    var chipsHeightPx by remember { androidx.compose.runtime.mutableIntStateOf(0) }
    val density = androidx.compose.ui.platform.LocalDensity.current
    val carouselExpandedHeightPx = remember { with(density) { 220.dp.toPx() } }
    val spacer12Px = remember { with(density) { 12.dp.toPx() } }
    val maxCollapsePx = remember { with(density) { 140.dp.toPx() } }
    
    val topHeightPx = headerHeightPx + searchHeightPx + chipsHeightPx + (spacer12Px * 2) +
        (if (state.placed.isNotEmpty()) carouselExpandedHeightPx else 0f)

    val gridState = androidx.compose.foundation.lazy.grid.rememberLazyGridState()
    
    // We don't need the complex translation logic anymore. The user wants the header
    // to scroll away naturally with the grid to make more room.

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxSize()
                .dottedPaper()
                .statusBarsPadding()
        ) {
            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Adaptive(minSize = 116.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 100.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                // The entire header area goes here as a full-span item so it scrolls away
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                    Column(Modifier.fillMaxWidth()) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text("MinecraftWidget", style = MaterialTheme.typography.headlineMedium, color = Obsidian)
                                Spacer(Modifier.height(6.dp))
                                Caption("Put a Minecraft entity on your home screen")
                            }
                        }

                        if (state.placed.isNotEmpty()) {
                            PlacedWidgetsCarousel(placed = state.placed, onEdit = onEditWidget)
                        }

                        MinecraftSearchField(
                            value = state.query,
                            onValueChange = onQuery,
                            placeholder = "Search Minecraft entities",
                            modifier = Modifier.fillMaxWidth(),
                        )

                        Spacer(Modifier.height(12.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            item {
                                MinecraftChip(
                                    label = "Animated",
                                    selected = state.animatedOnly,
                                    onClick = { onAnimatedOnly(!state.animatedOnly) },
                                    icon = Icons.Default.PlayArrow,
                                )
                            }
                            item {
                                MinecraftChip(
                                    label = "All entities",
                                    selected = state.category == null,
                                    onClick = { onCategory(null) },
                                )
                            }
                            items(listOf("Blocks", "Structures", "Mobs", "Items", "Player")) { cat ->
                                MinecraftChip(
                                    label = cat,
                                    selected = state.category == cat,
                                    onClick = { onCategory(if (state.category == cat) null else cat) },
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                    }
                }

                if (state.loading) {
                    items(12) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .aspectRatio(0.8f)
                                .sticker(
                                    shape = MaterialTheme.shapes.medium,
                                    fill = com.example.minecraftwidget.ui.theme.StoneChalk,
                                    borderWidth = 2.dp,
                                    lift = 3.dp,
                                )
                        )
                    }
                } else if (state.results.isEmpty()) {
                    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                        val isCategoryEmpty = state.category != null && state.query.isBlank()
                        EmptyHint(
                            title = if (isCategoryEmpty) "Coming Soon" else "Nothing matches that",
                            detail = if (isCategoryEmpty) {
                                "${state.category} are coming soon! Check back in future updates."
                            } else if (state.query.isBlank()) {
                                "No entities found. Check back later or clear filters."
                            } else {
                                "No entities called \"${state.query}\" — try another name, or clear the filters."
                            },
                            action = {
                                MinecraftButton(
                                    text = if (isCategoryEmpty) "View all entities" else "Clear filters",
                                    onClick = {
                                        onQuery("")
                                        onCategory(null)
                                        onAnimatedOnly(false)
                                    },
                                    container = CardColor,
                                )
                            },
                        )
                    }

                } else {
                    items(state.results, key = { it.id }) { entry ->
                        EntityCell(entry, selected = false) { onOpenDetail(entry) }
                    }
                }
            }
        }
    }
}

/**
 * One Minecraft mob in the browse grid.
 *
 * Shared with the widget's own Minecraft mob picker so the two grids cannot drift apart — this
 * is the cell the user learns on the first screen and meets again while configuring.
 */
@Composable
fun EntityCell(entry: MinecraftEntity, selected: Boolean, onClick: () -> Unit) {
    val interactions = remember { MutableInteractionSource() }
    val fill = if (selected) com.example.minecraftwidget.ui.theme.StoneDark else CardColor
    val blockShape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp)
    Column(
        Modifier
            .pressScale(interactions)
            .sticker(
                shape = blockShape,
                fill = fill,
                borderWidth = if (selected) 3.dp else 2.dp,
                outline = if (selected) com.example.minecraftwidget.ui.theme.CreeperGreen else Obsidian,
                lift = if (selected) 5.dp else 3.dp,
            )
            .clickable(interactionSource = interactions, indication = null, onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SpriteStage(
            modifier = Modifier.fillMaxWidth().aspectRatio(1f),
            fill = WaterBlue,
            shape = blockShape,
            borderWidth = 2.dp,
            lift = 0.dp,
            ballFraction = 0.66f,
            inset = 8.dp,
        ) {
            EntityIcon(entry.id, entry.displayName, size = 64.dp)
        }
        Spacer(Modifier.height(8.dp))
        Text(
            entry.name,
            style = MaterialTheme.typography.labelMedium,
            color = Obsidian,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            entry.variant ?: ("#" + entry.id.toString().padStart(3, '0')),
            style = MaterialTheme.typography.bodySmall,
            color = ObsidianSoft,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(2.dp))
    }
}

/** Skeleton cells while the bundled catalog is still being parsed off the main thread. */
@Composable
private fun LoadingGrid() {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 116.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        userScrollEnabled = false,
        modifier = Modifier.fillMaxSize()
    ) {
        items(12) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.8f)
                    .sticker(
                        shape = MaterialTheme.shapes.medium,
                        fill = com.example.minecraftwidget.ui.theme.StoneChalk,
                        borderWidth = 2.dp,
                        lift = 3.dp,
                    ),
            )
        }
    }
}









