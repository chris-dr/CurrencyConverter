package com.drevnitskaya.currencyconverter.data.repository

import android.util.Log
import com.drevnitskaya.currencyconverter.data.entities.CurrencyRateWrapper
import com.drevnitskaya.currencyconverter.data.source.local.dao.CurrencyLocalSource
import com.drevnitskaya.currencyconverter.data.source.remote.CurrencyRemoteSource
import io.reactivex.Single
import io.reactivex.SingleSource
import io.reactivex.schedulers.Schedulers

interface CurrencyRepository {
    fun getCurrencyRate(currCode: String): Single<CurrencyRateWrapper>
}

class CurrencyRepositoryImpl(
    private val remoteDataSource: CurrencyRemoteSource,
    private val localDataSource: CurrencyLocalSource
) : CurrencyRepository {
    override fun getCurrencyRate(currCode: String): Single<CurrencyRateWrapper> {
        return remoteDataSource.getCurrencyRate(currCode)
            .map { remoteRate ->
                localDataSource.updateRate(remoteRate)
                CurrencyRateWrapper(rate = remoteRate, fromCache = false)
            }
            .onErrorResumeNext {
                Log.w(javaClass.canonicalName, "Remote rate fetching has failed")
                return@onErrorResumeNext SingleSource {
                    val localRate = localDataSource.getRate()
                    CurrencyRateWrapper(rate = localRate, fromCache = true)
                }
            }
            .subscribeOn(Schedulers.io())
    }
}