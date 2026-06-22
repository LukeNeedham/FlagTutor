package com.flagtutor.app.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.flagtutor.app.domain.model.CountryBoundaries
import com.flagtutor.app.domain.model.CountryBoundaries.LatLng

private const val PADDING_FRACTION = 0.05f

@Composable
fun CountryMapHighlight(
    alpha2Code: String,
    modifier: Modifier = Modifier,
    highlightColor: Color = MaterialTheme.colorScheme.primary,
    landColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
) {
    val boundaries = remember { CountryBoundaries.boundaries }
    val targetCode = remember(alpha2Code) { alpha2Code.lowercase() }

    Canvas(modifier = modifier) {
        val padX = size.width * PADDING_FRACTION
        val padY = size.height * PADDING_FRACTION
        val drawW = size.width - 2 * padX
        val drawH = size.height - 2 * padY

        val viewport = WORLD_VIEWPORT

        for ((code, polygons) in boundaries) {
            if (code == "aq") continue
            val fill = if (code == targetCode) highlightColor else landColor
            for (ring in polygons) {
                val path = buildPath(ring, viewport, padX, padY, drawW, drawH)
                drawPath(path, fill, style = Fill)
                drawPath(path, borderColor, style = Stroke(width = 0.5f))
            }
        }
    }
}

private data class Viewport(
    val minLon: Float,
    val maxLon: Float,
    val minLat: Float,
    val maxLat: Float,
)

private val WORLD_VIEWPORT = Viewport(-180f, 180f, -60f, 85f)

private fun DrawScope.buildPath(
    ring: List<LatLng>,
    vp: Viewport,
    padX: Float,
    padY: Float,
    drawW: Float,
    drawH: Float,
): Path {
    val path = Path()
    val rangeX = vp.maxLon - vp.minLon
    val rangeY = vp.maxLat - vp.minLat

    ring.forEachIndexed { i, pt ->
        val x = padX + ((pt.lon - vp.minLon) / rangeX) * drawW
        val y = padY + ((vp.maxLat - pt.lat) / rangeY) * drawH
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}
