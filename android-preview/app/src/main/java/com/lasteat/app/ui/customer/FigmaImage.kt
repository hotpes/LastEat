package com.lasteat.app.ui.customer

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import com.caverock.androidsvg.SVG
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.ceil

private val imageCache = ConcurrentHashMap<String, ImageBitmap>()

/** Reads original local Figma exports. SVG source dimensions and viewBox stay unchanged. */
@Composable
internal fun figmaBitmap(file: String): ImageBitmap {
    val assets = LocalContext.current.assets
    val density = LocalDensity.current.density
    return remember(file, density) {
        imageCache.getOrPut("$file@$density") {
            if (file.endsWith(".svg")) {
                val svg = SVG.getFromAsset(assets, "figma/$file")
                val picture = svg.renderToPicture()
                val bitmap = Bitmap.createBitmap(
                    ceil(picture.width * density).toInt().coerceAtLeast(1),
                    ceil(picture.height * density).toInt().coerceAtLeast(1),
                    Bitmap.Config.ARGB_8888,
                )
                android.graphics.Canvas(bitmap).apply {
                    scale(density, density)
                    drawPicture(picture)
                }
                bitmap.asImageBitmap()
            } else assets.open("figma/$file").use { BitmapFactory.decodeStream(it).asImageBitmap() }
        }
    }
}

@Composable
internal fun FigmaImage(file: String, description: String?, modifier: Modifier = Modifier,
    crop: Boolean = false) {
    Image(figmaBitmap(file), description, modifier,
        contentScale = if (crop) ContentScale.Crop else ContentScale.Fit)
}
