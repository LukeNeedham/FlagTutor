package com.flagtutor.app.di

import android.content.Context
import com.flagtutor.app.data.crash.CrashRepository
import com.flagtutor.app.data.crash.CrashRepositoryImpl
import com.flagtutor.app.data.stats.DatabaseBuilderFactory
import com.flagtutor.app.data.stats.FlagAttemptRepository
import com.flagtutor.app.data.stats.FlagAttemptRepositoryImpl
import com.flagtutor.app.data.stats.VexedDatabase
import com.flagtutor.app.data.stats.createDatabase
import org.koin.dsl.module

fun androidModule(context: Context) = module {
    single<CrashRepository> { CrashRepositoryImpl(context) }
    single { createDatabase(DatabaseBuilderFactory(context).create()) }
    single { get<VexedDatabase>().flagAttemptDao() }
    single<FlagAttemptRepository> { FlagAttemptRepositoryImpl(get()) }
}
