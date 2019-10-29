package com.drevnitskaya.currencyconverter.domain

import com.drevnitskaya.currencyconverter.data.entities.CurrencyRate
import com.drevnitskaya.currencyconverter.data.repository.CurrencyRepository
import io.reactivex.Single

interface FetchRatesUseCase {
    fun execute(currencyCode: String): Single<CurrencyRate>
}

class FetchRatesUseCaseImpl(
    private val currencyRepository: CurrencyRepository
) : FetchRatesUseCase {
    override fun execute(currencyCode: String): Single<CurrencyRate> {
        return currencyRepository.getCurrencyRates(currencyCode)
    }
}