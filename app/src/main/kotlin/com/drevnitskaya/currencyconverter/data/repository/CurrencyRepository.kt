package com.drevnitskaya.currencyconverter.data.repository

import com.drevnitskaya.currencyconverter.data.entities.CurrRateResponse
import com.drevnitskaya.currencyconverter.data.source.remote.RemoteDataSource
import io.reactivex.Single

interface CurrencyRepository {
    fun getCurrencyRate(currCode: String): Single<CurrRateResponse>
}

class CurrencyRepositoryImpl(
    private val remoteDataSource: RemoteDataSource
) : CurrencyRepository {
    override fun getCurrencyRate(currCode: String): Single<CurrRateResponse> {
        return remoteDataSource.getCurrencyRate(currCode)
    }
}