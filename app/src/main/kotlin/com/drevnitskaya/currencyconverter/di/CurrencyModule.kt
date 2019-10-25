package com.drevnitskaya.currencyconverter.di

import com.drevnitskaya.currencyconverter.data.repository.CurrencyRepository
import com.drevnitskaya.currencyconverter.data.repository.CurrencyRepositoryImpl
import org.koin.dsl.module

val currencyModule = module {
    factory<CurrencyRepository> { CurrencyRepositoryImpl(remoteDataSource = get()) }
}