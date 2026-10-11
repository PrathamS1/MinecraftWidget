package com.example.minecraftwidget.data

import android.content.Context
import android.util.Log
import com.example.minecraftwidget.catalog.SpriteKey
import com.example.minecraftwidget.catalog.SpriteSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * What [SpriteSource.spriteParts] found.
 */
sealed interface SpriteFetch {
    class Ok(val parts: List<ByteArray>, val exact: Boolean) : SpriteFetch
    object Missing : SpriteFetch
    object Offline : SpriteFetch
}

/**
 * Loads Minecraft mob sprites from local assets.
 */
class SpriteSource(context: Context) {

    private val appContext = context.applicationContext
    private val spriteDir = File(appContext.filesDir, "sprites").apply { mkdirs() }

    // ---- Sprites ---------------------------------------------------------------

    fun cachedSpriteFile(key: SpriteKey, ext: String, part: Int = 0): File =
        File(spriteDir, if (part == 0) "${key.cacheName()}.$ext" else "${key.cacheName()}-f$part.$ext")

    fun isSpriteCached(key: SpriteKey, ext: String): Boolean = true

    fun isSpriteCached(key: SpriteKey): Boolean = true

    /**
     * Reads sprite bytes directly from bundled assets.
     */
    suspend fun spriteParts(set: SpriteSet, key: SpriteKey): SpriteFetch {
        return try {
            val bytes = withContext(Dispatchers.IO) {
                appContext.assets.open(set.path).use { it.readBytes() }
            }
            SpriteFetch.Ok(listOf(bytes), exact = true)
        } catch (e: Exception) {
            SpriteFetch.Missing
        }
    }

    suspend fun spriteBytes(set: SpriteSet, key: SpriteKey): ByteArray? =
        (spriteParts(set, key) as? SpriteFetch.Ok)?.parts?.firstOrNull()

    fun spriteUrl(set: SpriteSet, key: SpriteKey): Any? {
        return "file:///android_asset/${set.path}"
    }

    // ---- Cache management ------------------------------------------------------

    fun cacheSizeBytes(): Long =
        spriteDir.walkTopDown()
            .filter { it.isFile }
            .sumOf { it.length() }

    fun clearCache() {
        spriteDir.listFiles()?.forEach { it.delete() }
    }

    companion object {
        const val TAG = "SpriteSource"
        val SPRITE_EXTS = listOf("png", "webp")
    }
}
