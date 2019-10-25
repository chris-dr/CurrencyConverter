package com.drevnitskaya.currencyconverter.data.entities

import androidx.room.Entity
import com.drevnitskaya.currencyconverter.framework.db.TABLE_NAME_CURRENCY_RATE
import com.google.gson.annotations.SerializedName

@Entity(tableName = TABLE_NAME_CURRENCY_RATE)
data class CurrencyRate(
    @SerializedName("base")
    var currency: String? = null,
    var rates: Map<String, Float>
)