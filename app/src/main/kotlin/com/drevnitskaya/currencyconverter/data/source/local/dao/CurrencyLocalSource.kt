package com.drevnitskaya.currencyconverter.data.source.local.dao

import androidx.room.*
import com.drevnitskaya.currencyconverter.data.entities.CurrencyRate
import com.drevnitskaya.currencyconverter.framework.db.TABLE_NAME_CURRENCY_RATE

@Dao
interface CurrencyLocalSource {
    @Transaction
    fun updateRate(rate: CurrencyRate) {
        clearRates()
        insert(rate)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(rate: CurrencyRate)

    @Query("SELECT * FROM $TABLE_NAME_CURRENCY_RATE")
    fun getRate(): CurrencyRate

    @Query("DELETE FROM $TABLE_NAME_CURRENCY_RATE")
    fun clearRates()
}