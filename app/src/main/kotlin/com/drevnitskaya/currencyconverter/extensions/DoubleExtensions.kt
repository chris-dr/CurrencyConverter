package com.drevnitskaya.currencyconverter.extensions

fun Double.round(decimals: Int = 2): Double = "%.${decimals}f".format(this).toDouble()