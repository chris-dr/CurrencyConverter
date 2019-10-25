package com.drevnitskaya.currencyconverter.application

import android.app.Application
import com.drevnitskaya.currencyconverter.BuildConfig
import com.drevnitskaya.currencyconverter.di.appModule
import com.drevnitskaya.currencyconverter.di.currencyModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.module.Module

class CurrencyConverterApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin()
    }

    private fun initKoin() {
        startKoin {
            if (BuildConfig.DEBUG) {
                androidLogger()
            }
            androidContext(this@CurrencyConverterApp)
            modules(getKoinModules())
        }
    }

    private fun getKoinModules(): List<Module> {
        return listOf(appModule, currencyModule)
    }
}