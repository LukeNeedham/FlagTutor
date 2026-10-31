package com.flagtutor.app.di

import com.flagtutor.app.data.settings.LocalStorageThemePreferenceStore
import com.flagtutor.app.data.settings.ThemePreferenceStore
import com.flagtutor.app.data.stats.FlagAttemptRepository
import com.flagtutor.app.data.stats.LocalStorageFlagAttemptRepository
import org.koin.dsl.module

object WebModule {
    fun bindings() = module {
        single<ThemePreferenceStore> { LocalStorageThemePreferenceStore() }
        single<FlagAttemptRepository> { LocalStorageFlagAttemptRepository() }
    }
}
