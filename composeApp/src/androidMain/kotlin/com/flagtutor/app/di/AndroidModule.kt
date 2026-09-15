package com.flagtutor.app.di

import com.flagtutor.app.data.crash.CrashRepository
import com.flagtutor.app.data.crash.CrashRepositoryImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidModule = module {
    single<CrashRepository> { CrashRepositoryImpl(androidContext()) }
}
