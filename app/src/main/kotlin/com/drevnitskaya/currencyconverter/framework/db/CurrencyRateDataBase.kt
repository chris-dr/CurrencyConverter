package com.drevnitskaya.currencyconverter.framework.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.drevnitskaya.currencyconverter.data.entities.CurrencyRate
import com.drevnitskaya.currencyconverter.data.source.local.dao.CurrencyLocalSource

const val DATA_BASE_NAME = "currency_converter_room_db"
const val TABLE_NAME_CURRENCY_RATE = "currency_rate"

@Database(entities = [CurrencyRate::class], version = 1)
@TypeConverters(value = [HashMapConverter::class])
abstract class CurrencyRateDataBase : RoomDatabase() {
    abstract fun currencyDao(): CurrencyLocalSource
}