package com.flagtutor.app.di

import com.flagtutor.app.data.image.FlagImagePrefetcher
import com.flagtutor.app.data.local.CountryLocalDataSource
import com.flagtutor.app.data.remote.CountryApi
import com.flagtutor.app.data.repository.CountryRepository
import com.flagtutor.app.ui.feature.about.AboutViewModel
import com.flagtutor.app.ui.feature.game.GameViewModel
import com.flagtutor.app.ui.feature.home.HomeViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }
    singleOf(::CountryApi)
    singleOf(::CountryLocalDataSource)
    singleOf(::CountryRepository)
    singleOf(::FlagImagePrefetcher)
    viewModelOf(::HomeViewModel)
    viewModelOf(::AboutViewModel)
    viewModelOf(::GameViewModel)
}
