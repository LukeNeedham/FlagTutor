package com.flagtutor.app.di

import android.content.Context
import com.flagtutor.app.data.stats.DatabaseBuilderFactory
import com.flagtutor.app.data.settings.DataStoreThemePreferenceStore
import com.flagtutor.app.data.settings.ThemePreferenceStore
import com.flagtutor.app.data.stats.FlagAttemptRepository
import com.flagtutor.app.data.stats.FlagAttemptRepositoryImpl
import com.flagtutor.app.data.stats.AppDatabase
import com.flagtutor.app.data.stats.build
import org.koin.dsl.module

object AndroidModule {
    fun bindings(context: Context) = module {
        single<ThemePreferenceStore> { DataStoreThemePreferenceStore(context) }
        single { DatabaseBuilderFactory(context) }
        single { get<DatabaseBuilderFactory>().build() }
        single { get<AppDatabase>().flagAttemptDao() }
        single<FlagAttemptRepository> { FlagAttemptRepositoryImpl(get()) }
    }
}
