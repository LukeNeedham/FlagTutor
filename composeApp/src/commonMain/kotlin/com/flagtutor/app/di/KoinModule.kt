package com.flagtutor.app.di

import com.flagtutor.app.data.local.WikipediaLinkDataSource
import com.flagtutor.app.data.repository.CountryRepository
import com.flagtutor.app.ui.feature.about.AboutViewModel
import com.flagtutor.app.ui.feature.crashes.CrashesViewModel
import com.flagtutor.app.ui.feature.debug.DebugViewModel
import com.flagtutor.app.ui.feature.pickcountrynamegame.PickCountryNameGameViewModel
import com.flagtutor.app.ui.feature.home.HomeViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    singleOf(::WikipediaLinkDataSource)
    singleOf(::CountryRepository)
    viewModelOf(::HomeViewModel)
    viewModelOf(::AboutViewModel)
    viewModelOf(::DebugViewModel)
    viewModelOf(::CrashesViewModel)
    viewModelOf(::PickCountryNameGameViewModel)
}
