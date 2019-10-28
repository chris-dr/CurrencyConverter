package com.drevnitskaya.currencyconverter.extensions

import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

fun EditText.showSoftKeyboard() {
    val imm = this.context.getSystemService(Context.INPUT_METHOD_SERVICE)
    (imm as InputMethodManager).showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}