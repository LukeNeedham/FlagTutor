package com.flagtutor.app.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
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

        val viewport = computeViewport(boundaries, targetCode)

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

private fun computeViewport(
    boundaries: Map<String, List<List<LatLng>>>,
    targetCode: String,
): Viewport {
    val targetPolygons = boundaries[targetCode]
    if (targetPolygons == null || targetPolygons.isEmpty()) {
        return Viewport(-180f, 180f, -60f, 85f)
    }

    var minLon = Float.MAX_VALUE
    var maxLon = Float.MIN_VALUE
    var minLat = Float.MAX_VALUE
    var maxLat = Float.MIN_VALUE
    for (ring in targetPolygons) {
        for (pt in ring) {
            if (pt.lon < minLon) minLon = pt.lon
            if (pt.lon > maxLon) maxLon = pt.lon
            if (pt.lat < minLat) minLat = pt.lat
            if (pt.lat > maxLat) maxLat = pt.lat
        }
    }

    val spanLon = (maxLon - minLon).coerceAtLeast(1f)
    val spanLat = (maxLat - minLat).coerceAtLeast(1f)
    val margin = maxOf(spanLon, spanLat) * 3f

    val cLon = (minLon + maxLon) / 2f
    val cLat = (minLat + maxLat) / 2f

    val halfSpan = (maxOf(spanLon, spanLat) + margin) / 2f

    return Viewport(
        minLon = (cLon - halfSpan).coerceAtLeast(-180f),
        maxLon = (cLon + halfSpan).coerceAtMost(180f),
        minLat = (cLat - halfSpan).coerceAtLeast(-85f),
        maxLat = (cLat + halfSpan).coerceAtMost(85f),
    )
}

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
