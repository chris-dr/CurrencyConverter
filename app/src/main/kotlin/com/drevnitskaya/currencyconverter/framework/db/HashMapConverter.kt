package com.drevnitskaya.currencyconverter.framework.db

import androidx.room.TypeConverter
import com.drevnitskaya.currencyconverter.extensions.parse
import com.drevnitskaya.currencyconverter.extensions.toJson

class HashMapConverter {
    @TypeConverter
    fun fromString(value: String): HashMap<String, Float>? {
        return value.parse()
    }

    @TypeConverter
    fun fromHashMap(hashMap: HashMap<String, Float>?): String {
        return hashMap.toJson()
    }
}