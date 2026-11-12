package com.example.minermerge.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.dp
import com.example.minermerge.R
import com.example.minermerge.model.GamePhase
import kotlin.math.ceil
import kotlin.math.sin

/**
 * The original game's backdrop: sky-cyan base with a rolling terrain of
 * grass/dirt/stone blocks behind everything. The terrain drifts at 0.3x the
 * speed of the mine camera (parallax), matching the original's
 * bgParallaxSpeed tweak (default 0.3).
 *
 * This replaces the old approach of stretching bg_wallpaper (which is
 * actually the lucky "?" block texture) across the shaft.
 */
@Composable
fun BackgroundTerrain(
    cameraScrollY: Float,
    manualScrollY: Float,
    phase: GamePhase,
    scrollMode: Boolean,
    modifier: Modifier = Modifier
) {
    val grass = imageResource(R.drawable.block_grass)
    val dirt = imageResource(R.drawable.block_dirt)
    val stone = imageResource(R.drawable.block_stone)

    Canvas(modifier = modifier.fillMaxSize()) {
        val blockPx = 28.dp.toPx()

        // How far the mine content has scrolled, in px, so the terrain can
        // trail it at 0.3x. Camera scroll is in physics units (64 per block);
        // the shaft renders a block at (screen width / 5) px.
        val contentScrollPx = if (phase == GamePhase.DROPPING) {
            cameraScrollY * (size.width / 5f / 64f)
        } else if (scrollMode) {
            manualScrollY.dp.toPx()
        } else {
            0f
        }
        val parallaxPx = contentScrollPx * 0.3f

        val cols = ceil(size.width / blockPx).toInt() + 1
        for (c in 0 until cols) {
            // Deterministic rolling hills: same silhouette every frame.
            val hillTop = size.height * 0.30f +
                (sin(c * 0.55f) * 0.04f + sin(c * 0.23f + 1.7f) * 0.05f) * size.height
            val x = c * blockPx
            var y = hillTop - parallaxPx
            var row = 0
            while (y < size.height + blockPx) {
                val texture: ImageBitmap = when {
                    row == 0 -> grass
                    row <= 4 -> dirt
                    else -> stone
                }
                drawImage(
                    image = texture,
                    dstOffset = Offset(x, y),
                    dstSize = Size(blockPx, blockPx),
                    filterQuality = FilterQuality.None
                )
                y += blockPx
                row++
            }
        }
    }
}
