package com.drevnitskaya.currencyconverter.di

import com.drevnitskaya.currencyconverter.data.repository.CurrencyRepository
import com.drevnitskaya.currencyconverter.data.repository.CurrencyRepositoryImpl
import com.drevnitskaya.currencyconverter.domain.FetchRatesUseCase
import com.drevnitskaya.currencyconverter.domain.FetchRatesUseCaseImpl
import com.drevnitskaya.currencyconverter.presentation.currencyrate.CurrencyRateViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val currencyModule = module {
    factory<CurrencyRepository> {
        CurrencyRepositoryImpl(
            remoteDataSource = get()
        )
    }

    factory<FetchRatesUseCase> { FetchRatesUseCaseImpl(currencyRepository = get()) }

    viewModel {
        CurrencyRateViewModel(networkStateProvider = get(), fetchRatesUseCase = get())
    }
}