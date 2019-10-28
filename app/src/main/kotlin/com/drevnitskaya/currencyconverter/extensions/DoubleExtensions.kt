package com.drevnitskaya.currencyconverter.extensions

import java.text.DecimalFormat

private val formatter = DecimalFormat("#0.00")
fun Double.format(): String {
    return formatter.format(this)
}