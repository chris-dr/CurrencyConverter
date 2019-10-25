package com.drevnitskaya.currencyconverter.domain

import com.drevnitskaya.currencyconverter.data.entities.CurrencyRate
import com.drevnitskaya.currencyconverter.data.repository.CurrencyRepository
import io.reactivex.Single

interface FetchRateUseCase {
    fun execute(currencyCode: String): Single<CurrencyRate>
}

class FetchRateUseCaseImpl(
    private val currencyRepository: CurrencyRepository
) : FetchRateUseCase {
    override fun execute(currencyCode: String): Single<CurrencyRate> {
        return currencyRepository.getInitialCurrencyRate(currencyCode)
    }
}