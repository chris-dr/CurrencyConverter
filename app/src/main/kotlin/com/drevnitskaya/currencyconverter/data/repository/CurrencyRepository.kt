package com.drevnitskaya.currencyconverter.data.repository

import com.drevnitskaya.currencyconverter.data.entities.CurrencyRate
import com.drevnitskaya.currencyconverter.data.source.local.dao.CurrencyLocalSource
import com.drevnitskaya.currencyconverter.data.source.remote.CurrencyRemoteSource
import io.reactivex.Single

interface CurrencyRepository {
    fun getCurrencyRate(currCode: String): Single<CurrencyRate>
}

class CurrencyRepositoryImpl(
    private val remoteDataSource: CurrencyRemoteSource,
    private val localDataSource: CurrencyLocalSource
) : CurrencyRepository {
    override fun getCurrencyRate(currCode: String): Single<CurrencyRate> {
        return remoteDataSource.getCurrencyRate(currCode)
    }
}