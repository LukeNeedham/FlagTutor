package com.flagtutor.app.data.repository

import com.flagtutor.app.data.local.IdenticalFlagDataSource
import com.flagtutor.app.domain.model.Country
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async

/**
 * Loads the game data and the images for the first two flags as soon as the app starts, so the
 * first game doesn't have to wait for them after the user taps start.
 *
 * Must be called from the main thread.
 */
class GamePreloader(
    private val countryRepository: CountryRepository,
    private val identicalFlagDataSource: IdenticalFlagDataSource,
    private val flagImageRepository: FlagImageRepository,
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var initial: Deferred<List<Country>?>? = null

    /** Begins preloading if it hasn't started yet. */
    fun start() {
        if (initial != null) return
        prepareNext()
    }

    /** Discards any earlier preparation and starts loading two new countries for the next game. */
    fun prepareNext() {
        initial?.cancel()
        initial = scope.async {
            try {
                // Cached by the data sources, so the game itself doesn't fetch them on first start.
                identicalFlagDataSource.getIdenticalFlags()
                val picks = countryRepository.getCountries().shuffled().take(INITIAL_COUNT)
                picks.forEach { flagImageRepository.preload(it.alpha2Code) }
                picks
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                null
            }
        }
    }

    /**
     * The countries whose assets were preloaded, to be used for the first game. Returns them only
     * once; later games (and failed preloads) get null and choose countries themselves.
     */
    suspend fun takeInitialCountries(): List<Country>? {
        start()
        val pending = initial ?: return null
        initial = scope.async { null }
        return pending.await()?.takeIf { it.size == INITIAL_COUNT }
    }

    private companion object {
        const val INITIAL_COUNT = 2
    }
}
