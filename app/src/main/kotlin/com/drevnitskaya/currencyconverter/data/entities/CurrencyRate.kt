package com.drevnitskaya.currencyconverter.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.drevnitskaya.currencyconverter.framework.db.TABLE_NAME_CURRENCY_RATE
import com.google.gson.annotations.SerializedName

@Entity(tableName = TABLE_NAME_CURRENCY_RATE)
data class CurrencyRate(
    @SerializedName("base")
    @PrimaryKey
    var currency: String? = null,
    var rates: Map<String, Float>? = null
)

data class CurrencyRateWrapper(
    var rate: CurrencyRate? = null,
    var fromCache: Boolean = false
)