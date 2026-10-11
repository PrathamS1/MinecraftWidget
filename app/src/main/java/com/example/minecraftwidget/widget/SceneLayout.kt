package com.example.minecraftwidget.widget

/**
 * Layout bounds for widget rendering.
 */
object SceneLayout {

    data class Box(val left: Int, val top: Int, val width: Int, val height: Int) {
        val right: Int get() = left + width
        val bottom: Int get() = top + height
    }

    data class Layout(
        val entities: Box,
        val anchorBottom: Boolean = false,
    )

    fun layout(width: Int, height: Int): Layout {
        return Layout(entities = Box(0, 0, width, height), anchorBottom = false)
    }
}
