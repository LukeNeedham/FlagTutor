package com.flagtutor.app.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** The destinations of the app, one per page: [Home] is `HomePage`, and so on. */
@Serializable
sealed interface Route : NavKey {
    @Serializable
    data object Home : Route

    @Serializable
    data object PickCountryNameGame : Route

    /** Debug only: the game with [alpha2Code] as the first flag. Has no browser route. */
    @Serializable
    data class DebugPickCountryNameGame(val alpha2Code: String) : Route

    @Serializable
    data object Credits : Route

    @Serializable
    data object Settings : Route

    /** The app's own debug page. It links to [Route.Debug], the shared library's debug page. */
    @Serializable
    data object AppDebug : Route

    /** The shared library's debug page: animation speed, and links to its logs and crashes pages. */
    @Serializable
    data object Debug : Route

    @Serializable
    data object CountriesOverview : Route

    @Serializable
    data object ExcludedCountries : Route

    @Serializable
    data object FlagAttempts : Route

    @Serializable
    data object Logs : Route

    @Serializable
    data object Crashes : Route
}
