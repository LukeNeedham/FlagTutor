package com.flagtutor.app.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.flagtutor.app.domain.model.CountryBoundaries
import com.flagtutor.app.domain.model.CountryBoundaries.LatLng
import flagtutor.composeapp.generated.resources.Res
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.ExperimentalResourceApi

private const val PADDING_FRACTION = 0.05f
private const val MIN_COUNTRY_FRACTION = 1f / 3f
private const val MAX_COUNTRY_FRACTION = 5f / 6f
private const val LARGE_SPAN_THRESHOLD = 20f
private const val LARGE_SPAN_CAP = 120f

@OptIn(ExperimentalResourceApi::class)
@Composable
fun CountryMapHighlight(
    alpha2Code: String,
    modifier: Modifier = Modifier,
    highlightColor: Color = MaterialTheme.colorScheme.primary,
    landColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
) {
    var loaded by remember { mutableStateOf(CountryBoundaries.isLoaded) }

    LaunchedEffect(Unit) {
        if (!CountryBoundaries.isLoaded) {
            withContext(Dispatchers.Default) {
                val bytes = Res.readBytes("files/country_boundaries.json")
                CountryBoundaries.load(bytes)
            }
            loaded = true
        }
    }

    if (!loaded) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(
                text = "Loading map…",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    val boundaries = CountryBoundaries.boundaries
    val targetCode = remember(alpha2Code) { alpha2Code.lowercase() }
    val hasBoundary = remember(targetCode) { targetCode in boundaries }

    if (!hasBoundary) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(
                text = "No map data",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    Canvas(modifier = modifier) {
        val padX = size.width * PADDING_FRACTION
        val padY = size.height * PADDING_FRACTION
        val drawW = size.width - 2 * padX
        val drawH = size.height - 2 * padY

        if (drawW <= 0f || drawH <= 0f) return@Canvas

        val viewport = computeViewport(boundaries, targetCode, drawW / drawH)

        for ((code, polygons) in boundaries) {
            if (code == "aq" || code == targetCode) continue
            for (ring in polygons) {
                val path = buildPath(ring, viewport, padX, padY, drawW, drawH)
                drawPath(path, landColor, style = Fill)
                drawPath(path, borderColor, style = Stroke(width = 1f))
            }
        }
        boundaries[targetCode]?.forEach { ring ->
            val path = buildPath(ring, viewport, padX, padY, drawW, drawH)
            drawPath(path, highlightColor, style = Fill)
            drawPath(path, borderColor, style = Stroke(width = 1f))
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
    canvasAspectRatio: Float,
): Viewport {
    val targetPolygons = boundaries[targetCode]
        ?: return Viewport(-180f, 180f, -60f, 85f)

    var minLon = Float.MAX_VALUE
    var maxLon = -Float.MAX_VALUE
    var minLat = Float.MAX_VALUE
    var maxLat = -Float.MAX_VALUE
    for (ring in targetPolygons) {
        for (pt in ring) {
            if (pt.lon < minLon) minLon = pt.lon
            if (pt.lon > maxLon) maxLon = pt.lon
            if (pt.lat < minLat) minLat = pt.lat
            if (pt.lat > maxLat) maxLat = pt.lat
        }
    }

    var countrySpanLon = maxLon - minLon
    var cLon: Float

    if (countrySpanLon > 180f) {
        var wrapMin = Float.MAX_VALUE
        var wrapMax = -Float.MAX_VALUE
        for (ring in targetPolygons) {
            for (pt in ring) {
                val lon = if (pt.lon < 0f) pt.lon + 360f else pt.lon
                if (lon < wrapMin) wrapMin = lon
                if (lon > wrapMax) wrapMax = lon
            }
        }
        countrySpanLon = wrapMax - wrapMin
        cLon = (wrapMin + wrapMax) / 2f
        if (cLon > 180f) cLon -= 360f
    } else {
        cLon = (minLon + maxLon) / 2f
    }

    countrySpanLon = countrySpanLon.coerceAtLeast(2f)
    val countrySpanLat = (maxLat - minLat).coerceAtLeast(2f)
    val cLat = (minLat + maxLat) / 2f

    val maxSpan = maxOf(countrySpanLon, countrySpanLat)
    val t = ((maxSpan - LARGE_SPAN_THRESHOLD) / (LARGE_SPAN_CAP - LARGE_SPAN_THRESHOLD))
        .coerceIn(0f, 1f)
    val fraction = MIN_COUNTRY_FRACTION + t * (MAX_COUNTRY_FRACTION - MIN_COUNTRY_FRACTION)

    val neededLon = countrySpanLon / fraction
    val neededLat = countrySpanLat / fraction

    val finalSpanLon: Float
    val finalSpanLat: Float

    if (neededLon / neededLat > canvasAspectRatio) {
        finalSpanLon = neededLon
        finalSpanLat = neededLon / canvasAspectRatio
    } else {
        finalSpanLat = neededLat
        finalSpanLon = neededLat * canvasAspectRatio
    }

    return Viewport(
        minLon = cLon - finalSpanLon / 2f,
        maxLon = cLon + finalSpanLon / 2f,
        minLat = cLat - finalSpanLat / 2f,
        maxLat = cLat + finalSpanLat / 2f,
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
    val vpCenter = (vp.minLon + vp.maxLon) / 2f

    val refLon = ring.firstOrNull()?.lon ?: 0f
    val ringShift = when {
        refLon < vpCenter - 180f -> 360f
        refLon > vpCenter + 180f -> -360f
        else -> 0f
    }

    ring.forEachIndexed { i, pt ->
        val x = padX + ((pt.lon + ringShift - vp.minLon) / rangeX) * drawW
        val y = padY + ((vp.maxLat - pt.lat) / rangeY) * drawH
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}
