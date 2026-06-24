package com.flagtutor.app.ui.navigation

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class Destination : Parcelable {

    @Parcelize
    data object Home : Destination()

    @Parcelize
    data object About : Destination()

    @Parcelize
    data object PickCountryNameGame : Destination()

    @Parcelize
    data object PickFlagGame : Destination()

    @Parcelize
    data object Debug : Destination()
}
