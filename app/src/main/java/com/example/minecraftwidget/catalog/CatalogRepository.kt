package com.example.minecraftwidget.catalog

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/**
 * Reads the generated catalog out of assets.
 */
class CatalogRepository private constructor(context: Context) {

    private val appContext = context.applicationContext
    private val json = Json { ignoreUnknownKeys = true }
    private val loadLock = Mutex()

    @Volatile private var entities: List<MinecraftEntity>? = null
    @Volatile private var setIndex: SpriteSetIndex? = null

    suspend fun entities(): List<MinecraftEntity> = entities ?: loadLock.withLock {
        entities ?: withContext(Dispatchers.IO) {
            val text = appContext.assets.open("catalog.json").bufferedReader().use { it.readText() }
            json.decodeFromString<Catalog>(text).entities.also { entities = it }
        }
    }

    suspend fun sets(): List<SpriteSet> = setIndex()?.sets ?: emptyList()

    suspend fun setIndex(): SpriteSetIndex? = setIndex ?: loadLock.withLock {
        setIndex ?: withContext(Dispatchers.IO) {
            val text = appContext.assets.open("sets.json").bufferedReader().use { it.readText() }
            json.decodeFromString<SpriteSetIndex>(text)
                .let { it.copy(sets = it.sets.distinctBy { s -> s.id }) }
                .also { setIndex = it }
        }
    }

    suspend fun set(id: String): SpriteSet? = sets().firstOrNull { it.id == id }

    suspend fun entry(id: Int): MinecraftEntity? = entities().firstOrNull { it.id == id }

    /** Sets that can render this entity at all, animated ones first. */
    suspend fun setsFor(entityId: Int): List<SpriteSet> =
        sets().filter { entityId in it.frontIds }

    /** Ids of every entity at least one animated sprite set can render. */
    suspend fun animatedEntityIds(): Set<Int> =
        sets().filter { it.animated }
            .flatMap { it.frontIds.asSequence().toList() }
            .toHashSet()

    companion object {
        @Volatile private var instance: CatalogRepository? = null

        fun get(context: Context): CatalogRepository =
            instance ?: synchronized(this) {
                instance ?: CatalogRepository(context).also { instance = it }
            }
    }
}



