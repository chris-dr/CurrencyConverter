package com.drevnitskaya.currencyconverter.di

import com.drevnitskaya.currencyconverter.data.repository.CurrencyRepository
import com.drevnitskaya.currencyconverter.data.repository.CurrencyRepositoryImpl
import com.drevnitskaya.currencyconverter.domain.FetchRateUseCase
import com.drevnitskaya.currencyconverter.domain.FetchRateUseCaseImpl
import com.drevnitskaya.currencyconverter.presentation.currencyrate.CurrencyRateViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val currencyModule = module {
    factory<CurrencyRepository> {
        CurrencyRepositoryImpl(
            remoteDataSource = get()
        )
    }

    factory<FetchRateUseCase> { FetchRateUseCaseImpl(currencyRepository = get()) }

    viewModel {
        CurrencyRateViewModel(fetchRateUseCase = get())
    }
}