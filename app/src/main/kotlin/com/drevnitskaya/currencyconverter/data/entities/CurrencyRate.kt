package com.drevnitskaya.currencyconverter.data.entities

import com.google.gson.annotations.SerializedName

data class CurrencyRate(
    @SerializedName("base")
    var currency: String = "",
    var rates: Map<String, Double>? = null
)

data class CurrencyItemWrapper(
    var currencyCode: String = "",
    var amount: Double = 100.0,
    var isSelected: Boolean = false
)