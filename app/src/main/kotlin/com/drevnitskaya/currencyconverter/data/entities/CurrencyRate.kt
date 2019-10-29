package com.drevnitskaya.currencyconverter.data.entities

import com.google.gson.annotations.SerializedName

const val DEFAULT_BASE_AMOUNT = 100.0

data class CurrencyRate(
    @SerializedName("base")
    var currency: String = "",
    var rates: Map<String, Double>? = null
)

data class CurrencyConversionItem(
    var currencyCode: String = "",
    var amount: Double = DEFAULT_BASE_AMOUNT,
    var isSelected: Boolean = false
)