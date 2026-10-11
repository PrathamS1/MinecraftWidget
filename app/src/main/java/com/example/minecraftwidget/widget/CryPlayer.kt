package com.example.minecraftwidget.widget

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.util.Log
import com.example.minecraftwidget.catalog.CatalogRepository
import kotlinx.coroutines.*
import kotlin.coroutines.resume

class CryHandle(val job: Job, val started: Deferred<Unit>)

object CryPlayer {

    private const val TAG = "CryPlayer"
    private const val MAX_WAIT_MS = 5_000L

    private val attributes: AudioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var playing: Job? = null

    @Synchronized
    fun play(context: Context, entityId: Int, legacy: Boolean = false): CryHandle {
        playing?.cancel()
        val appContext = context.applicationContext
        val started = CompletableDeferred<Unit>()
        val job = scope.launch {
            try {
                sound(appContext, entityId, started)
            } finally {
                started.complete(Unit)
            }
        }
        playing = job
        return CryHandle(job, started)
    }

    suspend fun preload(context: Context, entityId: Int, legacy: Boolean = false) {
        // No-op for now. Assets load fast enough, and MediaPlayer prepares fast enough.
    }

    private suspend fun sound(
        context: Context,
        entityId: Int,
        started: CompletableDeferred<Unit>,
    ) {
        val audio = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return

        if (audio.getStreamVolume(AudioManager.STREAM_MUSIC) == 0) {
            Log.d(TAG, "media volume is zero — skipping cry")
            return
        }

        val catalog = CatalogRepository.get(context)
        val entity = catalog.entities().find { it.id == entityId }
        val sounds = entity?.sounds
        if (sounds.isNullOrEmpty()) {
            Log.d(TAG, "No sounds for entity $entityId")
            return
        }

        val soundAssetPath = sounds.random()

        val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
            .setAudioAttributes(attributes)
            .build()

        if (audio.requestAudioFocus(focusRequest) != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            return
        }

        try {
            withTimeoutOrNull(MAX_WAIT_MS) {
                suspendCancellableCoroutine { continuation ->
                    val player = MediaPlayer()
                    var finished = false
                    val finish = {
                        if (!finished) {
                            finished = true
                            runCatching { player.release() }
                            if (continuation.isActive) continuation.resume(Unit)
                        }
                    }
                    try {
                        player.setAudioAttributes(attributes)
                        val afd = context.assets.openFd(soundAssetPath)
                        player.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                        afd.close()
                        
                        player.setOnCompletionListener { finish() }
                        player.setOnErrorListener { _, what, extra ->
                            Log.w(TAG, "MediaPlayer error $what/$extra")
                            finish()
                            true
                        }
                        player.setOnPreparedListener {
                            it.start()
                            started.complete(Unit)
                        }
                        player.prepareAsync()
                    } catch (e: Exception) {
                        Log.w(TAG, "could not play sound", e)
                        finish()
                    }
                    continuation.invokeOnCancellation { finish() }
                }
            }
        } finally {
            withContext(NonCancellable) { audio.abandonAudioFocusRequest(focusRequest) }
        }
    }
}
