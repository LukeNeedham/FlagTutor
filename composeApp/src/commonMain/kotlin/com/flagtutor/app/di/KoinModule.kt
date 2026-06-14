package com.flagtutor.app.di

import com.flagtutor.app.ui.feature.about.AboutViewModel
import com.flagtutor.app.ui.feature.game.GameViewModel
import com.flagtutor.app.ui.feature.home.HomeViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    viewModelOf(::HomeViewModel)
    viewModelOf(::AboutViewModel)
    viewModelOf(::GameViewModel)
}
