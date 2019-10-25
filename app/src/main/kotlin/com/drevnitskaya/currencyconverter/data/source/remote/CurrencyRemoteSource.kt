package com.drevnitskaya.currencyconverter.data.source.remote

import com.drevnitskaya.currencyconverter.data.entities.CurrencyRate
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface CurrencyRemoteSource {
    @GET("latest")
    fun getCurrencyRate(@Query("base") currCode: String): Single<CurrencyRate>
}