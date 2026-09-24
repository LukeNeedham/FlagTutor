package com.flagtutor.app.ui.navigation

import androidx.navigation3.runtime.NavKey
import com.flagtutor.app.data.crash.CrashEntry
import kotlinx.serialization.Serializable

@Serializable
sealed interface Destination : NavKey {

    @Serializable
    data object Home : Destination

    @Serializable
    data object PickCountryNameGame : Destination

    @Serializable
    data object Credits : Destination

    @Serializable
    data object Debug : Destination

    @Serializable
    data object CountriesOverview : Destination

    @Serializable
    data object FlagAttempts : Destination

    @Serializable
    data object Crashes : Destination

    @Serializable
    data class CrashDetail(val crash: CrashEntry) : Destination
}
