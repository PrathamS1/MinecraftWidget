package com.example.minecraftwidget.ui

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.minecraftwidget.ui.screens.MainScreen
import com.example.minecraftwidget.ui.theme.StoneLight
import com.example.minecraftwidget.ui.theme.MinecraftWidgetTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // The app is light on every screen, so the system bars are asked for dark icons
        // once here rather than being left to follow the device theme and disappear into
        // the cream stock on a phone set to dark mode.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        setContent {
            MinecraftWidgetTheme {
                Surface(color = StoneLight) {
                    val viewModel: MainViewModel = viewModel()
                    val state by viewModel.state.collectAsState()

                    // A widget can be placed or removed from the home screen while the
                    // app sits in the background, so re-read on every resume.
                    LifecycleResumeEffect(Unit) {
                        viewModel.refreshPlaced()
                        viewModel.refreshCacheSize()
                        onPauseOrDispose { }
                    }

                    val editLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.StartActivityForResult(),
                    ) {
                        viewModel.refreshPlaced()
                    }

                    MainScreen(
                        state = state,
                        onQuery = viewModel::setQuery,
                        onCategory = viewModel::setCategory,
                        onAnimatedOnly = viewModel::setAnimatedOnly,
                        onOpenDetail = viewModel::openDetail,
                        onCloseDetail = viewModel::closeDetail,
                        onPin = viewModel::requestPin,
                        onEditWidget = { widgetId -> },
                        onClearCache = viewModel::clearCache,
                        onClearCrash = viewModel::clearCrashReport,
                        onPlayCry = viewModel::playCry,
                    )
                }
            }
        }
    }
}






