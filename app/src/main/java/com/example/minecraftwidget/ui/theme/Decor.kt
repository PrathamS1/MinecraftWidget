package com.example.minecraftwidget.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/**
 * The three drawing primitives the whole app is built out of.
 *
 * They exist as modifiers rather than as wrapper composables so any layout can adopt the
 * look without another node in the tree, and so a CardColor, a chip and a button can all be
 * demonstrably the same object at different sizes.
 */

/**
 * The outlined "sticker" surface: updated to a clean, subtle standard Material style.
 */
fun Modifier.sticker(
    shape: Shape,
    fill: Color = CardColor,
    outline: Color = Obsidian,
    borderWidth: Dp = 2.dp,
    lift: Dp = 4.dp,
): Modifier = this
    .shadow(elevation = lift, shape = shape, clip = false)
    .background(fill, shape)
    .border(borderWidth, outline, shape)

/**
 * A hard rule along the top edge.
 *
 * Used where a fixed footer sits over scrolling content. This one really is structure
 * rather than fake elevation — it marks where the scroll region ends, which is exactly
 * the job a divider is for, and a soft shadow next to 2dp keylines everywhere else would
 * be the odd one out.
 */
fun Modifier.topRule(color: Color = Obsidian, width: Dp = 2.dp): Modifier = drawBehind {
    drawRect(color = color, size = Size(size.width, width.toPx()))
}

/**
 * A simple subtle solid background for Minecraft styling, replacing the old dotted paper.
 */
@Composable
fun Modifier.dottedPaper(
    background: Color = StoneLight,
    dot: Color = StoneDark,
): Modifier {
    return this.background(background)
}

/**
 * A Minecraft shadow ellipse drawn beneath entities.
 *
 * Used as the plate a sprite stands on to give every Entity a visual footprint.
 */
fun DrawScope.drawMinecraftBase(
    strokePx: Float,
    top: Color = CreeperGreen,
    bottom: Color = Color.White,
    outline: Color = Obsidian,
) {
    val shadowWidth = size.width * 0.7f
    val shadowHeight = size.height * 0.25f
    val center = Offset(size.width / 2f, size.height - shadowHeight)
    drawOval(
        color = Color.Black.copy(alpha = 0.2f),
        topLeft = Offset(center.x - shadowWidth / 2f, center.y - shadowHeight / 2f),
        size = Size(shadowWidth, shadowHeight)
    )
}

val BlobShape = androidx.compose.foundation.shape.GenericShape { size, _ ->
    val cx = size.width / 2f
    val cy = size.height / 2f
    val rx = size.width / 2f
    val ry = size.height / 2f
    val factors = floatArrayOf(0.98f, 0.88f, 1.02f, 0.90f, 1.03f, 0.85f)
    val n = factors.size
    val pts = Array(n) { i ->
        val angle = (i * 2.0 * Math.PI / n).toFloat()
        Offset(
            cx + rx * factors[i] * kotlin.math.cos(angle),
            cy + ry * factors[i] * kotlin.math.sin(angle)
        )
    }
    val mid0 = Offset((pts[0].x + pts[1].x) / 2f, (pts[0].y + pts[1].y) / 2f)
    moveTo(mid0.x, mid0.y)
    for (i in 0 until n) {
        val next = pts[(i + 1) % n]
        val afterNext = pts[(i + 2) % n]
        val mid = Offset((next.x + afterNext.x) / 2f, (next.y + afterNext.y) / 2f)
        quadraticTo(next.x, next.y, mid.x, mid.y)
    }
    close()

}





