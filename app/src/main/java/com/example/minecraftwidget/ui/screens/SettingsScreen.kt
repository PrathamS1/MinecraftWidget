package com.example.minecraftwidget.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minecraftwidget.BuildConfig
import com.example.minecraftwidget.ui.components.MinecraftButton
import com.example.minecraftwidget.ui.components.MinecraftIconButton
import com.example.minecraftwidget.ui.theme.CreeperGreen
import com.example.minecraftwidget.ui.theme.DiamondBlue
import com.example.minecraftwidget.ui.theme.Obsidian
import com.example.minecraftwidget.ui.theme.ObsidianSoft

// Authentic Minecraft GUI Colors
private val McBgDark = Color(0xFF1E1E1F)          // Deep slate/bedrock background
private val McHeaderBar = Color(0xFF181819)        // Top bar
private val McSectionHeader = Color(0xFF262628)    // Section banner background
private val McBorderDark = Color(0xFF111112)       // Outer bevel/border
private val McBorderHighlight = Color(0xFF3C3C40)  // Highlight edge
private val McRowHover = Color(0xFF2B2B2E)         // Row hover/active
private val McTextYellow = Color(0xFFFFE043)       // Minecraft button yellow / title highlight
private val McTextGreen = Color(0xFF55FF55)        // Minecraft emerald text green
private val McTextAqua = Color(0xFF55FFFF)         // Minecraft diamond aqua
private val McTextMuted = Color(0xFFAAAAAA)        // Muted gray
private val McDivider = Color(0xFF333336)          // Inset horizontal divider

@Composable
fun SettingsScreen(
    cacheBytes: Long,
    onClearCache: () -> Unit,
    onBack: () -> Unit,
    crashReport: String? = null,
    onClearCrash: () -> Unit = {},
    message: String? = null,
    placedCount: Int = 0,
) {
    BackHandler(onBack = onBack)

    Column(
        Modifier
            .fillMaxSize()
            .background(McBgDark)
            .statusBarsPadding(),
    ) {
        // Minecraft Header Bar
        MinecraftGuiTopBar(title = "OPTIONS & SETTINGS", onBack = onBack)

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding(),
        ) {
            if (message != null) {
                MinecraftBannerNotice(message)
            }

            // 1. WIDGETS SECTION
            MinecraftSectionHeader(title = "ACTIVE WIDGETS", icon = Icons.Default.Widgets)
            MinecraftSettingRow(
                title = "Widgets on Launcher",
                subtitle = if (placedCount > 0) {
                    "$placedCount active widget(s) running on your home screen. Tap any widget to trigger its configured action."
                } else {
                    "No widgets placed. Long-press launcher or tap any mob in browse to place."
                },
                trailing = {
                    MinecraftStatBadge(
                        text = "$placedCount Placed",
                        color = if (placedCount > 0) McTextGreen else McTextMuted,
                    )
                },
            )

            // 2. STORAGE & ASSETS SECTION
            MinecraftSectionHeader(title = "STORAGE & ASSETS", icon = Icons.Default.Storage)
            MinecraftSettingRow(
                title = "Local Asset Storage",
                subtitle = "All mob sprite sheets, frame tables, and sound effects are bundled locally in the app APK. Zero internet connection or network download required.",
                trailing = {
                    MinecraftStatBadge(text = "100% Offline", color = McTextAqua)
                },
            )
            MinecraftSettingRow(
                title = "Temporary Cache",
                subtitle = "Render cache for home screen RemoteViews: %.2f MB".format(cacheBytes / 1_048_576.0),
                trailing = {
                    if (cacheBytes > 0) {
                        MinecraftButton(
                            text = "Clear",
                            onClick = onClearCache,
                            icon = Icons.Default.Delete,
                            container = Color(0xFF8B2020),
                        )
                    } else {
                        Text(
                            text = "0.0 MB",
                            style = MaterialTheme.typography.bodySmall,
                            color = McTextMuted,
                        )
                    }
                },
            )

            // 3. ENGINE & BATTERY SECTION
            MinecraftSectionHeader(title = "PERFORMANCE & BATTERY", icon = Icons.Default.BatteryChargingFull)
            MinecraftSettingRow(
                title = "Zero Wake Locks",
                subtitle = "Powered directly by native RemoteViews and ViewFlipper. No background services run while your device is asleep or screen is turned off.",
                trailing = {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = McTextGreen,
                        modifier = Modifier.size(20.dp),
                    )
                },
            )
            MinecraftSettingRow(
                title = "Frame Rate Pruning",
                subtitle = "Animations are pruned to ~10–14 FPS for classic pixel-art authenticity while staying well below Android's IPC memory limits.",
                trailing = {
                    MinecraftStatBadge(text = "10-14 FPS", color = McTextYellow)
                },
            )

            // 4. INTERACTION SYSTEM
            MinecraftSectionHeader(title = "TAP INTERACTIONS", icon = Icons.Default.TouchApp)
            MinecraftSettingRow(
                title = "Sound Effect",
                subtitle = "Plays authentic mob cries, hurt, ambient, or step sound effects immediately via low-latency SoundPool.",
            )
            MinecraftSettingRow(
                title = "Play Animation",
                subtitle = "Triggers the entity's complete sprite sheet animation sequence once upon tapping.",
            )
            MinecraftSettingRow(
                title = "Physical Wobble",
                subtitle = "Applies a playful squash-and-stretch wobble motion to static or animated mobs.",
            )

            // 5. DIAGNOSTICS (if crash exists)
            if (crashReport != null) {
                MinecraftSectionHeader(title = "DIAGNOSTICS & CRASH LOG", icon = Icons.Default.Info)
                MinecraftCrashSection(crashReport = crashReport, onClear = onClearCrash)
            }

            // 6. ABOUT & LEGAL SECTION
            MinecraftSectionHeader(title = "ABOUT MINECRAFTWIDGET", icon = Icons.Default.Layers)
            MinecraftSettingRow(
                title = "App Version",
                subtitle = "Version ${BuildConfig.VERSION_NAME} (Open-Source GitHub Edition)",
                trailing = {
                    Text(
                        text = "v${BuildConfig.VERSION_NAME}",
                        style = MaterialTheme.typography.labelMedium,
                        color = McTextYellow,
                    )
                },
            )
            MinecraftSettingRow(
                title = "Legal Disclaimer",
                subtitle = "NOT AN OFFICIAL MINECRAFT PRODUCT. NOT APPROVED BY OR ASSOCIATED WITH MOJANG OR MICROSOFT. Minecraft is a trademark of Mojang Synergies AB / Microsoft.",
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

/**
 * Top bar inspired by Minecraft GUI header styling with carved stone outline.
 */
@Composable
private fun MinecraftGuiTopBar(
    title: String,
    onBack: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(McHeaderBar)
            .minecraftBevel(raised = false)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MinecraftIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                onClick = onBack,
                container = Color(0xFF3C3C40),
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = McTextYellow,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * Minecraft section banner header spanning edge to edge.
 */
@Composable
private fun MinecraftSectionHeader(
    title: String,
    icon: ImageVector? = null,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(McSectionHeader)
            .minecraftBevel(raised = true)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (icon != null) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = McTextYellow,
                    modifier = Modifier.size(16.dp),
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = McTextYellow,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
            )
        }
    }
}

/**
 * Flat, list-based setting item row with subtle separators and hover/press states.
 */
@Composable
private fun MinecraftSettingRow(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .background(McBgDark)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = if (trailing != null) 12.dp else 0.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = McTextMuted,
                    lineHeight = 16.sp,
                )
            }
            trailing?.invoke()
        }
    }
    HorizontalDivider(color = McDivider, thickness = 1.dp)
}

/**
 * Stat badge styled like a Minecraft UI pill.
 */
@Composable
private fun MinecraftStatBadge(
    text: String,
    color: Color,
) {
    Box(
        modifier = Modifier
            .background(Color(0xFF141416), RoundedCornerShape(2.dp))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold,
        )
    }
}

/**
 * Notice banner with Minecraft gold text.
 */
@Composable
private fun MinecraftBannerNotice(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF2E2412))
            .border(1.dp, Color(0xFF856404))
            .padding(14.dp),
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = McTextYellow,
        )
    }
}

/**
 * Diagnostic crash reporting box in Minecraft terminal style.
 */
@Composable
private fun MinecraftCrashSection(
    crashReport: String,
    onClear: () -> Unit,
) {
    val clipboard = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF161618))
            .padding(16.dp),
    ) {
        Text(
            text = "An unexpected termination was logged on the previous run:",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White,
        )
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black)
                .border(1.dp, Color(0xFF444448))
                .padding(10.dp),
        ) {
            Text(
                text = crashReport.lineSequence().take(6).joinToString("\n"),
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFFF5555), // Minecraft Red
                maxLines = 6,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MinecraftButton(
                text = if (copied) "Copied!" else "Copy Log",
                onClick = {
                    clipboard.setText(AnnotatedString(crashReport))
                    copied = true
                },
                icon = Icons.Default.ContentCopy,
                container = Color(0xFF3C3C40),
            )
            MinecraftButton(
                text = "Clear",
                onClick = onClear,
                container = Color(0xFF8B2020),
            )
        }
    }
    HorizontalDivider(color = McDivider, thickness = 1.dp)
}

/**
 * Draws a subtle beveled border typical of Minecraft GUI stone blocks.
 */
private fun Modifier.minecraftBevel(raised: Boolean = true): Modifier = drawBehind {
    val strokeWidth = 2.dp.toPx()
    val topColor = if (raised) Color(0xFF4A4A50) else Color(0xFF101012)
    val bottomColor = if (raised) Color(0xFF101012) else Color(0xFF4A4A50)

    // Top border
    drawLine(
        color = topColor,
        start = Offset(0f, strokeWidth / 2),
        end = Offset(size.width, strokeWidth / 2),
        strokeWidth = strokeWidth,
    )
    // Bottom border
    drawLine(
        color = bottomColor,
        start = Offset(0f, size.height - strokeWidth / 2),
        end = Offset(size.width, size.height - strokeWidth / 2),
        strokeWidth = strokeWidth,
    )
}
