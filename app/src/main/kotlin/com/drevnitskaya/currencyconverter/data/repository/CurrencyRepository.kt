package com.drevnitskaya.currencyconverter.data.repository

import com.drevnitskaya.currencyconverter.data.entities.CurrencyRate
import com.drevnitskaya.currencyconverter.data.source.remote.CurrencyRemoteSource
import io.reactivex.Single

interface CurrencyRepository {
    fun getCurrencyRates(currencyCode: String): Single<CurrencyRate>
}

class CurrencyRepositoryImpl(
    private val remoteDataSource: CurrencyRemoteSource
) : CurrencyRepository {
    override fun getCurrencyRates(currencyCode: String): Single<CurrencyRate> {
        return remoteDataSource.getCurrencyRates(currencyCode)
    }
}