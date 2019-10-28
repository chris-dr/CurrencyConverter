package com.drevnitskaya.currencyconverter.extensions

import android.content.Context.INPUT_METHOD_SERVICE
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity

fun AppCompatActivity.hideSoftKeyboard() {
    if (currentFocus == null) {
        return
    }
    val imm = getSystemService(INPUT_METHOD_SERVICE)
    (imm as InputMethodManager).hideSoftInputFromWindow(currentFocus.windowToken, 0)
}