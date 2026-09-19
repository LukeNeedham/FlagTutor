package com.flagtutor.app.ui.navigation

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class Destination : Parcelable {

    @Parcelize
    data object Home : Destination()

    @Parcelize
    data object PickCountryNameGame : Destination()

    @Parcelize
    data object Credits : Destination()

    @Parcelize
    data object Debug : Destination()

    @Parcelize
    data object DebugData : Destination()

    @Parcelize
    data object FlagAttempts : Destination()

    @Parcelize
    data object Crashes : Destination()

    @Parcelize
    data object CrashDetail : Destination()
}
