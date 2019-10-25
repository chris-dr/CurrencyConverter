package com.drevnitskaya.currencyconverter.presentation.currencyrate

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.drevnitskaya.currencyconverter.R
import org.koin.android.viewmodel.ext.android.viewModel

class CurrencyRateActivity : AppCompatActivity() {
    private val viewModel: CurrencyRateViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_currency_rate)
    }
}
