package com.drevnitskaya.currencyconverter.di

import com.drevnitskaya.currencyconverter.data.repository.CurrencyRepository
import com.drevnitskaya.currencyconverter.data.repository.CurrencyRepositoryImpl
import com.drevnitskaya.currencyconverter.data.source.local.dao.CurrencyLocalSource
import com.drevnitskaya.currencyconverter.framework.db.CurrencyRateDataBase
import com.drevnitskaya.currencyconverter.presentation.currencyrate.CurrencyRateViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val currencyModule = module {
    single<CurrencyLocalSource> { get<CurrencyRateDataBase>().currencyDao() }

    factory<CurrencyRepository> {
        CurrencyRepositoryImpl(
            remoteDataSource = get(),
            localDataSource = get()
        )
    }

    viewModel { CurrencyRateViewModel() }
}