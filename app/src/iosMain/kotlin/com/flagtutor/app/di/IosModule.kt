package com.flagtutor.app.di

import com.flagtutor.app.data.stats.DatabaseBuilderFactory
import com.flagtutor.app.data.settings.UserDefaultsThemePreferenceStore
import com.flagtutor.app.data.settings.ThemePreferenceStore
import com.flagtutor.app.data.stats.FlagAttemptRepository
import com.flagtutor.app.data.stats.FlagAttemptRepositoryImpl
import com.flagtutor.app.data.stats.AppDatabase
import com.flagtutor.app.data.stats.build
import org.koin.dsl.module

object IosModule {
    fun bindings() = module {
        single<ThemePreferenceStore> { UserDefaultsThemePreferenceStore() }
        single { DatabaseBuilderFactory() }
        single { get<DatabaseBuilderFactory>().build() }
        single { get<AppDatabase>().flagAttemptDao() }
        single<FlagAttemptRepository> { FlagAttemptRepositoryImpl(get()) }
    }
}
