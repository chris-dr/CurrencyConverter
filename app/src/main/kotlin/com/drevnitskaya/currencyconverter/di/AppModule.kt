package com.drevnitskaya.currencyconverter.di

import com.drevnitskaya.currencyconverter.BuildConfig
import com.drevnitskaya.currencyconverter.data.source.remote.RemoteDataSource
import com.drevnitskaya.currencyconverter.framework.api.BaseOkHttpClientBuilder
import com.drevnitskaya.currencyconverter.framework.api.BASE_URL_CONVERTER
import com.drevnitskaya.currencyconverter.framework.api.BaseRetrofitClientFactory
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

const val DI_NAME_BASE_URL = "di_name_base_url"
const val DI_HTTP_LOG_INTERCEPTOR = "di_logging_interceptor"

val appModule = module {

    single(named(DI_NAME_BASE_URL)) { BASE_URL_CONVERTER }
    factory {
        BaseOkHttpClientBuilder().init()
    }

    factory<Interceptor>(named(DI_HTTP_LOG_INTERCEPTOR)) {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    single<List<Interceptor>> {
        if (BuildConfig.DEBUG) {
            listOf(get(named(DI_HTTP_LOG_INTERCEPTOR)))
        } else {
            emptyList()
        }
    }

    single<CallAdapter.Factory> { RxJava2CallAdapterFactory.create() }

    single<Converter.Factory> { GsonConverterFactory.create() }

    factory<RemoteDataSource> {
        BaseRetrofitClientFactory(
            baseOkHttpClientBuilder = get(),
            callAdapterFactory = get(),
            converterFactory = get(),
            baseUrl = get(named(DI_NAME_BASE_URL)),
            interceptors = get()
        ).build()
    }
}