package com.drevnitskaya.currencyconverter.testutils

import com.google.gson.Gson
import okhttp3.MediaType
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response

fun getMockedHttpException(code: Int, errorObject: Any? = null): HttpException {
    return HttpException(
        Response.error<Any>(
            code, ResponseBody.create(
                MediaType.parse("application/json"), errorObject?.toJson() ?: ""
            )
        )
    )
}

fun Any.toJson(): String = Gson().toJson(this)