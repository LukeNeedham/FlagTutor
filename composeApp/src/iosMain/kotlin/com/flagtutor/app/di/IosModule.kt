package com.flagtutor.app.di

import com.flagtutor.app.data.crash.CrashRepository
import com.flagtutor.app.data.crash.CrashRepositoryImpl
import com.flagtutor.app.data.stats.DatabaseBuilderFactory
import com.flagtutor.app.data.stats.FlagAttemptRepository
import com.flagtutor.app.data.stats.FlagAttemptRepositoryImpl
import com.flagtutor.app.data.stats.FlagTutorDatabase
import com.flagtutor.app.data.stats.createDatabase
import org.koin.dsl.module

fun iosModule() = module {
    single<CrashRepository> { CrashRepositoryImpl() }
    single { createDatabase(DatabaseBuilderFactory().create()) }
    single { get<FlagTutorDatabase>().flagAttemptDao() }
    single<FlagAttemptRepository> { FlagAttemptRepositoryImpl(get()) }
}
