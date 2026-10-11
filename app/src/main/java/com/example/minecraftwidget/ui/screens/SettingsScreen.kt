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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
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
import com.example.minecraftwidget.ui.theme.Obsidian
import com.example.minecraftwidget.ui.theme.ObsidianSoft
import com.example.minecraftwidget.ui.theme.StoneChalk
import com.example.minecraftwidget.ui.theme.StoneDark
import com.example.minecraftwidget.ui.theme.StoneLight

// Light Mode Stone Palette
private val LightBg = Color(0xFFF4F4F4)            // Clean light stone background
private val LightHeaderBar = Color(0xFFE8E8E8)      // Header bar stone tone
private val LightSectionHeader = Color(0xFFDEDEDE)  // Section banner background
private val LightDivider = Color(0xFFDDDDDD)        // Soft gray divider line
private val LightBadgeBg = Color(0xFFE2E2E2)        // Soft badge pill background
private val LightBadgeBorder = Color(0xFFC0C0C0)    // Muted badge border
private val AccentGreen = CreeperGreen              // Single cohesive accent green

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
            .background(LightBg)
            .statusBarsPadding(),
    ) {
        // Light Mode Header Top Bar
        LightGuiTopBar(title = "Settings", onBack = onBack)

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding(),
        ) {
            if (message != null) {
                LightBannerNotice(message)
            }

            // 1. ACTIVE WIDGETS
            LightSectionHeader(title = "ACTIVE WIDGETS", icon = Icons.Default.Widgets)
            LightSettingRow(
                title = "Widgets on Launcher",
                subtitle = if (placedCount > 0) {
                    "$placedCount active widget(s) currently placed on your home screen. Tap any widget to trigger its configured action."
                } else {
                    "No widgets placed yet. Long-press launcher or select a mob from the browse tab to add."
                },
                trailing = {
                    LightStatBadge(
                        text = "$placedCount Placed",
                        highlight = placedCount > 0,
                    )
                },
            )

            // 2. STORAGE & ASSETS
            LightSectionHeader(title = "STORAGE & ASSETS", icon = Icons.Default.Storage)
            LightSettingRow(
                title = "Local Asset Storage",
                subtitle = "All mob sprite sheets, frame tables, and sound effects are bundled locally in the app APK. Zero internet connection or network download required.",
                trailing = {
                    LightStatBadge(text = "100% Offline", highlight = true)
                },
            )
            LightSettingRow(
                title = "Temporary Cache",
                subtitle = "Render cache for home screen RemoteViews: %.2f MB".format(cacheBytes / 1_048_576.0),
                trailing = {
                    if (cacheBytes > 0) {
                        MinecraftButton(
                            text = "Clear",
                            onClick = onClearCache,
                            icon = Icons.Default.Delete,
                            container = StoneChalk,
                        )
                    } else {
                        Text(
                            text = "0.0 MB",
                            style = MaterialTheme.typography.bodySmall,
                            color = ObsidianSoft,
                        )
                    }
                },
            )

            // 3. PERFORMANCE & BATTERY
            LightSectionHeader(title = "PERFORMANCE & BATTERY", icon = Icons.Default.BatteryChargingFull)
            LightSettingRow(
                title = "Zero Wake Locks",
                subtitle = "Powered directly by native RemoteViews and ViewFlipper. No background services run while your device is asleep or screen is turned off.",
                trailing = {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = AccentGreen,
                        modifier = Modifier.size(20.dp),
                    )
                },
            )
            LightSettingRow(
                title = "Frame Rate Pruning",
                subtitle = "Animations are pruned to ~10–14 FPS for classic pixel-art authenticity while staying well below Android's IPC memory limits.",
                trailing = {
                    LightStatBadge(text = "10-14 FPS", highlight = false)
                },
            )

            // 4. TAP INTERACTIONS
            LightSectionHeader(title = "TAP INTERACTIONS", icon = Icons.Default.TouchApp)
            LightSettingRow(
                title = "Sound Effect",
                subtitle = "Plays authentic mob cries, ambient noises, or step sounds immediately via low-latency SoundPool.",
            )
            LightSettingRow(
                title = "Play Animation",
                subtitle = "Triggers the entity's complete sprite sheet animation sequence once upon tapping.",
            )
            LightSettingRow(
                title = "Physical Wobble",
                subtitle = "Applies a playful squash-and-stretch wobble motion to static or animated mobs.",
            )

            // 5. DIAGNOSTICS (if crash exists)
            if (crashReport != null) {
                LightSectionHeader(title = "DIAGNOSTICS & CRASH LOG", icon = Icons.Default.Info)
                LightCrashSection(crashReport = crashReport, onClear = onClearCrash)
            }

            // 6. ABOUT & LEGAL
            LightSectionHeader(title = "ABOUT MINECRAFTWIDGET", icon = Icons.Default.Layers)
            LightSettingRow(
                title = "App Version",
                subtitle = "Version ${BuildConfig.VERSION_NAME} (Open-Source GitHub Edition)",
                trailing = {
                    Text(
                        text = "v${BuildConfig.VERSION_NAME}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Obsidian,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
            )
            LightSettingRow(
                title = "Legal Disclaimer",
                subtitle = "NOT AN OFFICIAL MINECRAFT PRODUCT. NOT APPROVED BY OR ASSOCIATED WITH MOJANG OR MICROSOFT. Minecraft is a trademark of Mojang Synergies AB / Microsoft.",
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

/**
 * Top bar styled in clean light stone with subtle border.
 */
@Composable
private fun LightGuiTopBar(
    title: String,
    onBack: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(LightHeaderBar)
            .lightStoneBevel(raised = false)
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
                container = Color.White,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Obsidian,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * Section banner header spanning edge to edge in light tone.
 */
@Composable
private fun LightSectionHeader(
    title: String,
    icon: ImageVector? = null,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(LightSectionHeader)
            .lightStoneBevel(raised = true)
            .padding(horizontal = 16.dp, vertical = 9.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (icon != null) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = Obsidian,
                    modifier = Modifier.size(16.dp),
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = Obsidian,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
            )
        }
    }
}

/**
 * Flat list-based setting item row in light mode.
 */
@Composable
private fun LightSettingRow(
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
            .background(LightBg)
            .padding(horizontal = 16.dp, vertical = 13.dp),
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
                    color = Obsidian,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = ObsidianSoft,
                    lineHeight = 17.sp,
                )
            }
            trailing?.invoke()
        }
    }
    HorizontalDivider(color = LightDivider, thickness = 1.dp)
}

/**
 * Minimalist stat badge in light theme using neutral stone and optional accent green.
 */
@Composable
private fun LightStatBadge(
    text: String,
    highlight: Boolean = false,
) {
    Box(
        modifier = Modifier
            .background(if (highlight) AccentGreen.copy(alpha = 0.12f) else LightBadgeBg, RoundedCornerShape(3.dp))
            .border(
                1.dp,
                if (highlight) AccentGreen.copy(alpha = 0.5f) else LightBadgeBorder,
                RoundedCornerShape(3.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = if (highlight) AccentGreen else Obsidian,
            fontWeight = FontWeight.Bold,
        )
    }
}

/**
 * Informational notice banner in light neutral tone.
 */
@Composable
private fun LightBannerNotice(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(LightHeaderBar)
            .border(1.dp, LightDivider)
            .padding(14.dp),
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = Obsidian,
        )
    }
}

/**
 * Diagnostic crash reporting box in light mode.
 */
@Composable
private fun LightCrashSection(
    crashReport: String,
    onClear: () -> Unit,
) {
    val clipboard = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(LightBg)
            .padding(16.dp),
    ) {
        Text(
            text = "An unexpected termination was logged on the previous run:",
            style = MaterialTheme.typography.bodySmall,
            color = Obsidian,
        )
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFEEEEEE))
                .border(1.dp, LightDivider)
                .padding(10.dp),
        ) {
            Text(
                text = crashReport.lineSequence().take(6).joinToString("\n"),
                style = MaterialTheme.typography.bodySmall,
                color = Obsidian,
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
                container = Color.White,
            )
            MinecraftButton(
                text = "Clear",
                onClick = onClear,
                container = StoneChalk,
            )
        }
    }
    HorizontalDivider(color = LightDivider, thickness = 1.dp)
}

/**
 * Subtle border bevel lines for light stone aesthetic.
 */
private fun Modifier.lightStoneBevel(raised: Boolean = true): Modifier = drawBehind {
    val strokeWidth = 1.5.dp.toPx()
    val topColor = if (raised) Color(0xFFF7F7F7) else Color(0xFFD0D0D0)
    val bottomColor = if (raised) Color(0xFFD0D0D0) else Color(0xFFF7F7F7)

    // Top border line
    drawLine(
        color = topColor,
        start = Offset(0f, strokeWidth / 2),
        end = Offset(size.width, strokeWidth / 2),
        strokeWidth = strokeWidth,
    )
    // Bottom border line
    drawLine(
        color = bottomColor,
        start = Offset(0f, size.height - strokeWidth / 2),
        end = Offset(size.width, size.height - strokeWidth / 2),
        strokeWidth = strokeWidth,
    )
}
