package com.drevnitskaya.currencyconverter.data.entities

import com.google.gson.annotations.SerializedName

data class CurrencyRate(
    @SerializedName("base")
    var currency: String = "",
    var rates: LinkedHashMap<String, Float>? = null
)

data class CurrencyItemWrapper(
    var currencyCode: String = "",
    var amount: Float = 100f,
    var isSelected: Boolean = false
)