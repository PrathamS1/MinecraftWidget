package com.example.minecraftwidget.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import com.example.minecraftwidget.ui.theme.CreeperGreen
import kotlin.math.sin

@Composable
fun SwayingGrassBackground(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "grass_sway")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(modifier = modifier.fillMaxWidth().height(100.dp)) {
        val width = size.width
        val height = size.height
        
        // Base dirt/grass block
        drawRect(
            color = Color(0xFF3B2516), // Dirt brown
            topLeft = Offset(0f, height * 0.3f),
            size = Size(width, height * 0.7f)
        )
        drawRect(
            color = CreeperGreen.copy(alpha = 0.8f),
            topLeft = Offset(0f, height * 0.3f),
            size = Size(width, height * 0.15f)
        )

        // Swaying tall grass blades
        val bladeCount = (width / 15f).toInt()
        val path = Path()
        
        for (i in 0 until bladeCount) {
            val baseX = i * 15f + 5f
            val baseY = height * 0.4f
            
            // varying height and phase per blade
            val bladeHeight = 30f + (sin(i.toFloat()) * 10f)
            val tipXOffset = sin(phase + i) * 12f
            
            path.reset()
            path.moveTo(baseX, baseY)
            path.lineTo(baseX + tipXOffset, baseY - bladeHeight)
            path.lineTo(baseX + 8f, baseY)
            path.close()
            
            drawPath(
                path = path,
                color = CreeperGreen
            )
        }
    }
}
