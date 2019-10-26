package com.drevnitskaya.currencyconverter.data.entities

import androidx.annotation.StringRes
import com.drevnitskaya.currencyconverter.R

sealed class ErrorHolder(@StringRes val errorMsgResId: Int) {
    class NetworkError : ErrorHolder(R.string.shared_noNetworkError_msg)
    class GeneralError : ErrorHolder(R.string.shared_generalError_msg)
}