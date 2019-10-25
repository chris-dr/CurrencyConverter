package com.drevnitskaya.currencyconverter.data.repository

import com.drevnitskaya.currencyconverter.data.entities.CurrencyRate
import com.drevnitskaya.currencyconverter.data.source.remote.CurrencyRemoteSource
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

interface CurrencyRepository {
    fun getInitialCurrencyRate(currCode: String): Single<CurrencyRate>
}

class CurrencyRepositoryImpl(
    private val remoteDataSource: CurrencyRemoteSource
    ) : CurrencyRepository {
    override fun getInitialCurrencyRate(currCode: String): Single<CurrencyRate> {
        return remoteDataSource.getCurrencyRate(currCode)
            .subscribeOn(Schedulers.io())
    }
}