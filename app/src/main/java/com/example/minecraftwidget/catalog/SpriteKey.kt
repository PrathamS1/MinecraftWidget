package com.example.minecraftwidget.catalog

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MinecraftEntity(
    @SerialName("i") val id: Int,
    @SerialName("n") val name: String,
    @SerialName("c") val category: String,
    @SerialName("t") val tags: List<String> = emptyList(),
    @SerialName("f") val variant: String? = null,
    @SerialName("s") val slug: String = "",
    @SerialName("snd") val sounds: List<String> = emptyList(),
) {
    val displayName: String get() = if (variant.isNullOrBlank()) name else "$name ($variant)"

    fun matches(query: String): Boolean {
        if (query.isBlank()) return true
        val q = query.trim().lowercase()
        return name.lowercase().contains(q) ||
            slug.contains(q) ||
            variant?.lowercase()?.contains(q) == true ||
            category.lowercase().contains(q)
    }
}

@Serializable
data class Catalog(
    @SerialName("entities") val entities: List<MinecraftEntity>
)

data class SpriteKey(
    val setId: String,
    val entityId: Int,
    val back: Boolean = false,
    val shiny: Boolean = false,
    val female: Boolean = false,
    val style: String? = null,
) {
    fun cacheName(): String = buildString {
        append(setId)
        append('-')
        append(entityId)
        if (back) append("-b")
        if (shiny) append("-s")
        if (female) append("-f")
        style?.let { append('-').append(it) }
    }

    override fun toString(): String = cacheName()
}


