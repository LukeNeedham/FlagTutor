package com.flagtutor.app.di

import android.content.Context
import com.flagtutor.app.data.crash.CrashRepository
import com.flagtutor.app.data.crash.CrashRepositoryImpl
import org.koin.dsl.module

fun androidModule(context: Context) = module {
    single<CrashRepository> { CrashRepositoryImpl(context) }
}
