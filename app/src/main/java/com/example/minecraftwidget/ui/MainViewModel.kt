package com.example.minecraftwidget.ui

import android.app.Application
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.minecraftwidget.catalog.CatalogRepository
import com.example.minecraftwidget.catalog.MinecraftEntity
import com.example.minecraftwidget.catalog.SpriteKey
import com.example.minecraftwidget.catalog.SpriteSet
import com.example.minecraftwidget.data.CrashLog
import com.example.minecraftwidget.data.SpriteSource
import com.example.minecraftwidget.data.WidgetConfig
import com.example.minecraftwidget.data.WidgetConfigStore
import com.example.minecraftwidget.widget.CryPlayer
import com.example.minecraftwidget.widget.MobWidgetProvider
import com.example.minecraftwidget.widget.WidgetActions
import com.example.minecraftwidget.widget.WidgetRenderer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * A sprite set shown as a picture of one entity in it.
 */
data class SetPreview(val set: SpriteSet, val url: Any?, val shinyUrl: Any? = null)

/** A widget the user has already placed, so it can be edited from inside the app. */
data class PlacedWidget(
    val widgetId: Int,
    val config: WidgetConfig,
    val entry: MinecraftEntity?,
    val set: SpriteSet?,
)

data class MainUiState(
    val loading: Boolean = true,
    val query: String = "",
    val category: String? = null,
    val animatedOnly: Boolean = false,
    val results: List<MinecraftEntity> = emptyList(),
    val detail: MinecraftEntity? = null,
    val detailSets: List<SetPreview> = emptyList(),
    val placed: List<PlacedWidget> = emptyList(),
    val cacheBytes: Long = 0,
    val canPin: Boolean = false,
    /** A crash recorded on a previous run, for the tester to copy out. */
    val crashReport: String? = null,
    /** Something failed in a way the tester should hear about rather than guess at. */
    val message: String? = null,
)

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val catalog = CatalogRepository.get(app)
    private val source = SpriteSource(app)
    private val store = WidgetConfigStore(app)

    private val _state = MutableStateFlow(MainUiState())
    val state: StateFlow<MainUiState> = _state.asStateFlow()

    /** What the results list is currently filtered by. Drives [results] off the UI thread. */
    private data class Filters(
        val query: String = "",
        val category: String? = null,
        val animatedOnly: Boolean = false,
    )

    private val filters = MutableStateFlow(Filters())
    private val allEntities = MutableStateFlow<List<MinecraftEntity>>(emptyList())
    private val animatedIds = MutableStateFlow<Set<Int>>(emptySet())

    init {
        launchSafely("load the catalogue") {
            allEntities.value = catalog.entities()
            animatedIds.value = catalog.animatedEntityIds()
            _state.update {
                it.copy(
                    loading = false,
                    canPin = AppWidgetManager.getInstance(getApplication())
                        .isRequestPinAppWidgetSupported,
                )
            }
            refreshPlaced()
            refreshCacheSize()
            refreshCrashReport()
        }
        observeFilters()
    }

    /**
     * Recomputes the results list off the main thread, and only for the keystroke the
     * user actually stopped on.
     *
     * Filtering 1,345 entries is not expensive in isolation, but it was running
     * synchronously inside `setQuery` — so a six-letter search ran it six times on the UI
     * thread, each one competing with the recomposition and the icon decodes it had just
     * invalidated. That is what the dropped frames during search actually were.
     * `mapLatest` throws away the work for every intermediate keystroke.
     */
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun observeFilters() {
        launchSafely("filter the list") {
            combine(allEntities, animatedIds, filters) { all, ids, f -> Triple(all, ids, f) }
                // Typing gets a short settle; a chip tap is a deliberate single act and
                // should feel instant.
                .debounce { (_, _, f) -> if (f.query.isBlank()) 0L else QUERY_DEBOUNCE_MS }
                .mapLatest { (all, ids, f) ->
                    withContext(Dispatchers.Default) {
                        all.filter { entry ->
                            entry.matches(f.query) &&
                                (f.category == null || entry.category == f.category) &&
                                (!f.animatedOnly || entry.id in ids)
                        }
                    }
                }
                .collect { results -> _state.update { it.copy(results = results) } }
        }
    }

    fun setQuery(q: String) {
        // The text field must echo the keystroke immediately; only the *results* wait.
        _state.update { it.copy(query = q) }
        filters.update { it.copy(query = q) }
    }

    fun setCategory(cat: String?) {
        _state.update { it.copy(category = cat) }
        filters.update { it.copy(category = cat) }
    }

    fun setAnimatedOnly(only: Boolean) {
        _state.update { it.copy(animatedOnly = only) }
        filters.update { it.copy(animatedOnly = only) }
    }

    /**
     * Plays a Pokémon's cry, cancelling whichever one was still sounding.
     *
     * The cancelling is [CryPlayer]'s own rule now, and process-wide: this used to keep a
     * private `cryJob` while the widget's tap handler kept none, so scrolling the app
     * behaved and tapping a widget three times played three overlapping cries.
     */
    fun playCry(entityId: Int) {
        CryPlayer.play(getApplication(), entityId, legacy = LEGACY_CRY)
    }

    fun openDetail(entry: MinecraftEntity) {
        _state.update { it.copy(detail = entry, detailSets = emptyList()) }
        launchSafely("load that Pokémon") {
            val sets = catalog.setsFor(entry.id)
                .sortedWith(compareByDescending<SpriteSet> { it.animated }.thenBy { it.order })
                .map { set ->
                    SetPreview(
                        set = set,
                        url = source.spriteUrl(set, SpriteKey(set.id, entry.id)),
                        shinyUrl = if (set.covers(entry.id, back = false, shiny = true, female = false, style = null)) {
                            source.spriteUrl(set, SpriteKey(set.id, entry.id, shiny = true))
                        } else {
                            null
                        },
                    )
                }
            _state.update { it.copy(detailSets = sets) }
        }
        // Opening a Pokémon announces it. This is the tap the bug report is about: until
        // now, choosing a Pokémon anywhere in the app was completely silent.
        playCry(entry.id)
    }

    fun closeDetail() = _state.update { it.copy(detail = null, detailSets = emptyList()) }

    /**
     * Asks the launcher to place a widget directly, so the user never has to hunt through
     * the system widget tray. Supported since API 26; on launchers that decline, the
     * button is hidden rather than silently doing nothing.
     *
     * The success callback is not optional. These providers declare a configuration
     * activity, which means Android deliberately withholds the first APPWIDGET_UPDATE and
     * expects that activity to draw the widget — but a pinned widget never runs it. With
     * no callback the widget is placed and then simply never rendered, which is why
     * adding a Entity from inside the app produced an empty square on the home screen.
     */
    fun requestPin(config: WidgetConfig) {
        val context = getApplication<Application>()
        val manager = AppWidgetManager.getInstance(context)
        if (!manager.isRequestPinAppWidgetSupported) return

        launchSafely("add the widget") {
            // The pin API carries no payload and never runs the configuration activity,
            // so stash the choice for the provider to adopt when the widget arrives.
            store.putPendingPin(config)

            val provider = ComponentName(context, MobWidgetProvider.Medium::class.java)
            val placed = PendingIntent.getBroadcast(
                context,
                PIN_CALLBACK_REQUEST,
                Intent(context, MobWidgetProvider.Medium::class.java)
                    .setAction(WidgetActions.ACTION_PINNED),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            manager.requestPinAppWidget(provider, null, placed)
        }
    }

    fun refreshPlaced() {
        launchSafely("read your widgets") {
            val live = WidgetRenderer.allWidgetIds(getApplication()).toSet()
            val placed = store.knownWidgetIds()
                .filter { it in live }
                .map { id ->
                    val config = store.get(id)
                    PlacedWidget(id, config, catalog.entry(config.entityId), catalog.set(config.setId))
                }
            _state.update { it.copy(placed = placed) }
        }
    }

    fun refreshCacheSize() {
        launchSafely("measure the cache") {
            val bytes = withContext(Dispatchers.IO) { source.cacheSizeBytes() }
            _state.update { it.copy(cacheBytes = bytes) }
        }
    }

    fun clearCache() {
        launchSafely("clear the cache") {
            withContext(Dispatchers.IO) { source.clearCache() }
            refreshCacheSize()
        }
    }

    fun dismissMessage() = _state.update { it.copy(message = null) }

    // ---- Crash diagnostics -------------------------------------------------------

    fun refreshCrashReport() {
        _state.update { it.copy(crashReport = CrashLog.read(getApplication())) }
    }

    fun clearCrashReport() {
        CrashLog.clear(getApplication())
        _state.update { it.copy(crashReport = null) }
    }

    /**
     * `viewModelScope` dispatches on the main thread and has no exception handler, so an
     * unhandled throw in any coroutine launched from here closes the app. For a sideloaded
     * beta that is the worst of both worlds: the tester loses their place *and* learns
     * nothing. Every launch in this class goes through here instead, so the failure becomes
     * a logged line and a message on screen.
     */
    private fun launchSafely(what: String, block: suspend () -> Unit): Job =
        viewModelScope.launch {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "could not $what", e)
                _state.update { it.copy(message = "Couldn't $what. ${e.javaClass.simpleName}", loading = false) }
            }
        }

    private companion object {
        const val TAG = "MainViewModel"

        /**
         * Long enough to skip the intermediate states of a fast typist, short enough that
         * the grid still feels like it is keeping up.
         */
        const val QUERY_DEBOUNCE_MS = 120L

        /**
         * Prefer the GBA-era cry in the app, matching the widget's default so a Pokémon
         * sounds the same in both places. [SpriteSource.cryFile] falls back on its own
         * for the Pokémon that have no legacy recording.
         */
        const val LEGACY_CRY = true

        /** Request code for the pin callback; distinct from any widget id. */
        const val PIN_CALLBACK_REQUEST = 0x9101
    }
}




