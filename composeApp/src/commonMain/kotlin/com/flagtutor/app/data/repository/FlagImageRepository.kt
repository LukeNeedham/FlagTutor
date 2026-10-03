package com.flagtutor.app.data.repository

import androidx.compose.ui.graphics.ImageBitmap
import com.flagtutor.app.data.local.FlagColorDataSource
import com.flagtutor.app.ui.util.ExtractedColor
import com.flagtutor.app.ui.util.decodeImageBitmap
import flagtutor.composeapp.generated.resources.Res
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.jetbrains.compose.resources.ExperimentalResourceApi

// Enough to hold the current and upcoming country with room to spare, without keeping every
// decoded map bitmap in memory.
private const val MAX_CACHED_COUNTRIES = 6

/** Everything the game screen needs to show one country, decoded and ready to draw. */
class FlagAssets(
    val flag: ImageBitmap,
    /** Null when there is no map image for the country. */
    val map: ImageBitmap?,
    val colors: List<ExtractedColor>,
)

/**
 * Loads and caches the flag image, map image and colours for countries, so the next country's
 * assets can be fetched in advance and are ready the moment it is shown.
 *
 * Must be called from the main thread; loading itself runs on a background scope.
 */
class FlagImageRepository(private val flagColorDataSource: FlagColorDataSource) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val entries = LinkedHashMap<String, Deferred<FlagAssets>>()

    /** Starts loading [alpha2Code]'s assets in the background if they aren't already loading. */
    fun preload(alpha2Code: String) {
        entries.getOrPut(alpha2Code) { scope.async { loadAssets(alpha2Code) } }
        while (entries.size > MAX_CACHED_COUNTRIES) {
            val eldest = entries.keys.first()
            if (eldest == alpha2Code) break
            entries.remove(eldest)
        }
    }

    suspend fun load(alpha2Code: String): FlagAssets {
        preload(alpha2Code)
        val pending = entries.getValue(alpha2Code)
        return try {
            pending.await()
        } catch (e: Exception) {
            // Don't keep serving a failed load; a retry should start fresh.
            if (entries[alpha2Code] === pending) entries.remove(alpha2Code)
            throw e
        }
    }

    @OptIn(ExperimentalResourceApi::class)
    private suspend fun loadAssets(alpha2Code: String): FlagAssets = coroutineScope {
        // Each is a separate fetch (on web, a network request), so run them side by side.
        val flag = async { decodeImageBitmap(Res.readBytes("files/flags/$alpha2Code.png")) }
        val map = async {
            try {
                decodeImageBitmap(Res.readBytes("files/maps/${alpha2Code.lowercase()}.png"))
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                null
            }
        }
        val colors = async { flagColorDataSource.getColors(alpha2Code) }
        FlagAssets(flag = flag.await(), map = map.await(), colors = colors.await())
    }
}
