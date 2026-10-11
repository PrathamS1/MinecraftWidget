package com.example.minecraftwidget.ui.screens

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minecraftwidget.BuildConfig
import com.example.minecraftwidget.ui.components.Caption
import com.example.minecraftwidget.ui.components.MinecraftButton
import com.example.minecraftwidget.ui.components.MinecraftHeader
import com.example.minecraftwidget.ui.components.SectionHeader
import com.example.minecraftwidget.ui.theme.GrassGreen
import com.example.minecraftwidget.ui.theme.DiamondBlue
import com.example.minecraftwidget.ui.theme.Obsidian
import com.example.minecraftwidget.ui.theme.ObsidianSoft
import com.example.minecraftwidget.ui.theme.dottedPaper
import com.example.minecraftwidget.ui.theme.sticker
import com.example.minecraftwidget.ui.theme.CardColor as CardColor

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
    val context = LocalContext.current

    Column(
        Modifier
            .fillMaxSize()
            .dottedPaper()
            .statusBarsPadding(),
    ) {
        MinecraftHeader("Settings", onBack = onBack)

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 16.dp),
        ) {
            if (message != null) {
                Panel { Caption(message) }
                Spacer(Modifier.height(16.dp))
            }

            // 1. Placed Widgets Summary
            Panel {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SectionHeader("Active Widgets")
                    Text(
                        text = "$placedCount Placed",
                        style = MaterialTheme.typography.titleMedium,
                        color = GrassGreen,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.height(6.dp))
                Caption(
                    if (placedCount > 0) {
                        "You currently have $placedCount Minecraft widget(s) active on your launcher. Tap any placed widget on the home screen to trigger its configured action."
                    } else {
                        "No widgets placed yet. Long-press an empty spot on your home screen or tap any mob in the Browse tab and select 'Add to home screen'."
                    }
                )
            }

            Spacer(Modifier.height(16.dp))

            // 2. Storage & Offline Assets
            Panel {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SectionHeader("Storage & Offline Assets")
                    Text(
                        text = "100% Offline",
                        style = MaterialTheme.typography.labelLarge,
                        color = DiamondBlue,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.height(8.dp))
                Caption(
                    "All Minecraft mob sprite sheets, animations, and sound effects are bundled locally with the application. " +
                        "No internet connection, remote downloads, or network caching are ever required."
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Temporary Disk Cache",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Obsidian,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "%.1f MB (Assets bundled locally)".format(cacheBytes / 1_048_576.0),
                            style = MaterialTheme.typography.bodySmall,
                            color = ObsidianSoft,
                        )
                    }
                    if (cacheBytes > 0) {
                        MinecraftButton(
                            text = "Clear temp",
                            onClick = onClearCache,
                            icon = Icons.Default.Delete,
                            container = CardColor,
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // 3. Performance & Battery
            Panel {
                SectionHeader("Performance & Battery")
                Spacer(Modifier.height(6.dp))
                Caption(
                    "Widgets utilize native Android RemoteViews and ViewFlipper rendering. There are zero background services, " +
                        "zero wake locks, and 0% CPU consumption while your screen is off."
                )
                Spacer(Modifier.height(10.dp))
                Caption(
                    "Sprite sheets are optimized with frame pruning (decimated to 10–14 FPS), ensuring classic, crisp Minecraft " +
                        "animations that respect system memory budgets and preserve battery life."
                )
            }

            Spacer(Modifier.height(16.dp))

            // 4. Widget Interactions & Audio
            Panel {
                SectionHeader("Interactions & Audio")
                Spacer(Modifier.height(6.dp))
                Caption("Widgets support three customizable touch interactions configured per-widget:")
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "• Play Sound: Plays authentic Minecraft ambient, hurt, or idle audio clips.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Obsidian,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "• Play Animation: Triggers the entity's complete animation cycle once on tap.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Obsidian,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "• Wobble: Adds an energetic squash-and-stretch bounce to the mob.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Obsidian,
                )
            }

            if (crashReport != null) {
                Spacer(Modifier.height(16.dp))
                Panel { CrashPanel(crashReport, onClearCrash) }
            }

            Spacer(Modifier.height(16.dp))

            // 5. About & Open Source Information
            Panel {
                SectionHeader("About MinecraftWidget")
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Version ${BuildConfig.VERSION_NAME} (Open Source Release)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Obsidian,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(8.dp))
                Caption(
                    "MinecraftWidget brings Minecraft mobs to your Android home screen with animated pixel-art sprites, " +
                        "procedural idle motion, custom shape plates, and interactive sound effects."
                )
                Spacer(Modifier.height(10.dp))
                Caption(
                    "NOT AN OFFICIAL MINECRAFT PRODUCT. NOT APPROVED BY OR ASSOCIATED WITH MOJANG OR MICROSOFT. " +
                        "Minecraft is a trademark of Mojang Synergies AB / Microsoft."
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

/**
 * Diagnostic crash report panel.
 */
@Composable
private fun CrashPanel(report: String, onClear: () -> Unit) {
    val clipboard = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    SectionHeader("Last crash report")
    Caption(
        "The app closed unexpectedly on a previous run. You can copy this diagnostic report to report issues on GitHub.",
    )
    Spacer(Modifier.height(10.dp))
    Text(
        report.lineSequence().take(6).joinToString("\n"),
        style = MaterialTheme.typography.bodySmall,
        color = Obsidian,
        maxLines = 6,
        overflow = TextOverflow.Ellipsis,
    )
    Spacer(Modifier.height(12.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        MinecraftButton(
            text = if (copied) "Copied" else "Copy report",
            onClick = {
                clipboard.setText(AnnotatedString(report))
                copied = true
            },
            icon = Icons.Default.ContentCopy,
            container = CardColor,
        )
        Spacer(Modifier.width(10.dp))
        MinecraftButton(text = "Clear", onClick = onClear, container = CardColor)
    }
}

/** A settings group panel styled as a Minecraft card. */
@Composable
private fun Panel(content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .sticker(shape = MaterialTheme.shapes.medium, fill = CardColor, lift = 4.dp)
            .padding(16.dp),
    ) {
        content()
    }
}
