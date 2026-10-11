package com.example.minecraftwidget.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.minecraftwidget.catalog.CatalogRepository
import com.example.minecraftwidget.catalog.SpriteSet
import kotlinx.coroutines.delay

@Composable
fun SpriteImage(
    url: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    spriteSet: SpriteSet? = null,
) {
    if (url == null) {
        Box(modifier)
        return
    }
    if (url is Int) {
        Image(
            painter = androidx.compose.ui.res.painterResource(id = url),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Fit,
            alignment = alignment
        )
        return
    }

    if (url is String && url.startsWith("file:///android_asset/")) {
        val context = LocalContext.current
        val path = url.removePrefix("file:///android_asset/")
        var frames by remember(url) { mutableStateOf<List<android.graphics.Bitmap>>(emptyList()) }
        var frameIndex by remember(url) { mutableStateOf(0) }
        var singleBitmap by remember(url) { mutableStateOf<android.graphics.Bitmap?>(null) }

        LaunchedEffect(url, spriteSet) {
            try {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { context.assets.open(path).use { stream ->
                    val bmp = BitmapFactory.decodeStream(stream)
                    if (bmp != null) {
                        val frameCount = spriteSet?.frameCount
                        val frameW = spriteSet?.frameWidth
                        val frameH = spriteSet?.frameHeight
                        
                        if (frameCount != null && frameW != null && frameH != null && frameCount > 1) {
                            val cols = bmp.width / frameW
                            val rows = bmp.height / frameH
                            val extracted = mutableListOf<android.graphics.Bitmap>()
                            var count = 0
                            for (r in 0 until rows) {
                                for (c in 0 until cols) {
                                    if (count >= frameCount) break
                                    extracted.add(
                                        android.graphics.Bitmap.createBitmap(
                                            bmp, c * frameW, r * frameH, frameW, frameH
                                        )
                                    )
                                    count++
                                }
                            }
                            frames = extracted
                            bmp.recycle()
                        } else {
                            singleBitmap = bmp
                        }
                    }
                }
            } } catch (e: Exception) {
                // Ignore
            }
        }

        LaunchedEffect(frames) {
            if (frames.isEmpty()) return@LaunchedEffect
            var current = 0
            while (true) {
                frameIndex = current
                delay(100) // 10fps = 100ms per frame
                current = (current + 1) % frames.size
            }
        }

        val bitmapToDraw = frames.getOrNull(frameIndex) ?: singleBitmap
        if (bitmapToDraw != null) {
            Image(
                bitmap = bitmapToDraw.asImageBitmap(),
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = ContentScale.Fit,
                alignment = alignment,
                filterQuality = FilterQuality.None,
            )
            return
        }
    }

    AsyncImage(
        model = url,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Fit,
        alignment = alignment,
        filterQuality = FilterQuality.None,
    )
}

@Composable
fun EntityIcon(
    entityId: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    fallbackUrl: String? = null,
) {
    val context = LocalContext.current
    var url by remember(entityId) { mutableStateOf<Any?>(null) }
    var defaultSet by remember(entityId) { mutableStateOf<SpriteSet?>(null) }
    
    LaunchedEffect(entityId) {
        val repo = CatalogRepository.get(context)
        val sets = repo.setsFor(entityId)
        val firstSet = sets.firstOrNull()
        if (firstSet != null) {
            defaultSet = firstSet
            url = "file:///android_asset/${firstSet.path}"
        }
    }

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        if (url != null) {
            SpriteImage(
                url = url,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                spriteSet = defaultSet
            )
        } else {
            Box(
                Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Text("?", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
    }
}
