package com.flagtutor.app.di

import com.flagtutor.app.data.crash.CrashRepository
import com.flagtutor.app.data.crash.CrashRepositoryImpl
import com.flagtutor.app.data.stats.DatabaseBuilderFactory
import com.flagtutor.app.data.settings.UserDefaultsThemePreferenceStore
import com.flagtutor.app.data.settings.AnimationSpeedPreferenceStore
import com.flagtutor.app.data.settings.UserDefaultsAnimationSpeedPreferenceStore
import com.flagtutor.app.data.settings.ThemePreferenceStore
import com.flagtutor.app.data.stats.FlagAttemptRepository
import com.flagtutor.app.data.stats.FlagAttemptRepositoryImpl
import com.flagtutor.app.data.stats.AppDatabase
import com.flagtutor.app.data.stats.createDatabase
import org.koin.dsl.module

fun iosModule() = module {
    single<AnimationSpeedPreferenceStore> { UserDefaultsAnimationSpeedPreferenceStore() }
    single<ThemePreferenceStore> { UserDefaultsThemePreferenceStore() }
    single<CrashRepository> { CrashRepositoryImpl() }
    single { createDatabase(DatabaseBuilderFactory().create()) }
    single { get<AppDatabase>().flagAttemptDao() }
    single<FlagAttemptRepository> { FlagAttemptRepositoryImpl(get()) }
}
