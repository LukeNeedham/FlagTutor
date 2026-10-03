package com.flagtutor.app.di

import com.flagtutor.app.data.crash.CrashRepository
import com.flagtutor.app.data.crash.CrashRepositoryImpl
import com.flagtutor.app.data.stats.FlagAttemptRepository
import com.flagtutor.app.data.stats.LocalStorageFlagAttemptRepository
import org.koin.dsl.module

fun webModule() = module {
    single<CrashRepository> { CrashRepositoryImpl() }
    single<FlagAttemptRepository> { LocalStorageFlagAttemptRepository() }
}
