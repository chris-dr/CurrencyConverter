package com.drevnitskaya.currencyconverter.data.entities
import com.google.gson.annotations.SerializedName

data class CurrencyRate(
    @SerializedName("base")
    var currency: String = "",
    var rates: HashMap<String, Float>? = null
)