package com.limbe.hexamusicplayer.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Size
import com.limbe.hexamusicplayer.domain.model.Track

@Composable
fun rememberArtworkImageRequest(
    track: Track?,
    width: Dp,
    height: Dp,
    preferContentUriFallback: Boolean = true,
    cacheKeySuffix: String = "${width.value}x${height.value}"
): ImageRequest? {
    val safeTrack = track ?: return null
    val data = safeTrack.artworkUri ?: safeTrack.contentUri.takeIf { preferContentUriFallback } ?: return null
    val context = LocalContext.current
    val density = LocalDensity.current
    val widthPx = with(density) { width.roundToPx() }.coerceAtLeast(1)
    val heightPx = with(density) { height.roundToPx() }.coerceAtLeast(1)
    val cacheKey = "artwork-${safeTrack.id}-$cacheKeySuffix"

    return remember(data, widthPx, heightPx, cacheKey) {
        ImageRequest.Builder(context)
            .data(data)
            .size(Size(widthPx, heightPx))
            .memoryCacheKey(cacheKey)
            .diskCacheKey(cacheKey)
            .networkCachePolicy(CachePolicy.DISABLED)
            .build()
    }
}

@Composable
fun rememberArtworkImageRequest(
    data: String?,
    width: Dp,
    height: Dp,
    cacheKey: String
): ImageRequest? {
    if (data.isNullOrBlank()) return null
    val context = LocalContext.current
    val density = LocalDensity.current
    val widthPx = with(density) { width.roundToPx() }.coerceAtLeast(1)
    val heightPx = with(density) { height.roundToPx() }.coerceAtLeast(1)

    return remember(data, widthPx, heightPx, cacheKey) {
        ImageRequest.Builder(context)
            .data(data)
            .size(Size(widthPx, heightPx))
            .memoryCacheKey(cacheKey)
            .diskCacheKey(cacheKey)
            .networkCachePolicy(CachePolicy.DISABLED)
            .build()
    }
}
