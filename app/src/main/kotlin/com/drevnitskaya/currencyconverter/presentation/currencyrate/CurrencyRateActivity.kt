package com.drevnitskaya.currencyconverter.presentation.currencyrate

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.transition.TransitionManager
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.drevnitskaya.currencyconverter.R
import com.drevnitskaya.currencyconverter.presentation.currencyrate.adapter.CurrencyConversionAdapter
import kotlinx.android.synthetic.main.activity_currency_rate.*
import org.koin.android.viewmodel.ext.android.viewModel

class CurrencyRateActivity : AppCompatActivity() {
    private val viewModel: CurrencyRateViewModel by viewModel()
    private lateinit var adapterCurrency: CurrencyConversionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_currency_rate)
        initViews()
        initViewModel()
    }

    override fun onStart() {
        super.onStart()
        viewModel.resumeRateRefreshing()
    }

    override fun onStop() {
        super.onStop()
        viewModel.stopRateRefreshing()
    }

    private fun initViewModel() {
        viewModel.apply {
            showProgress.observe(this@CurrencyRateActivity, Observer { shouldShow ->
                currencyProgress.visibility = if (shouldShow) View.VISIBLE else View.GONE
            })
            showErrorState.observe(this@CurrencyRateActivity, Observer { error ->
                currencyErrorState.visibility = if (error != null) {
                    currencyErrorState.setText(error.errorMsgResId)
                    TransitionManager.beginDelayedTransition(currencyRoot)
                    View.VISIBLE
                } else {
                    View.GONE
                }
            })
            setCalculatedValues.observe(this@CurrencyRateActivity, Observer { rates ->
                if (currencyRecyclerView.visibility == View.GONE) {
                    TransitionManager.beginDelayedTransition(currencyRoot)
                    currencyRecyclerView.visibility = View.VISIBLE
                }
                adapterCurrency.items = rates

            })
            scrollToBaseCurrency.observe(this@CurrencyRateActivity, Observer {
                currencyRecyclerView.scrollToPosition(0)
            })
            showOfflineMode.observe(this@CurrencyRateActivity, Observer { shouldShow ->
                TransitionManager.beginDelayedTransition(currencyRoot)
                currencyOfflineMode.visibility = if (shouldShow) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            })
        }
    }

    private fun initViews() {
        adapterCurrency = CurrencyConversionAdapter(onCurrencyClicked = { item ->
            viewModel.onCurrencyClicked(item)
        }, onValueUpdated = { input ->
            viewModel.onValueUpdated(input)
        })
        currencyRecyclerView.apply {
            val llm = LinearLayoutManager(
                this@CurrencyRateActivity,
                LinearLayoutManager.VERTICAL, false
            )
            llm.isItemPrefetchEnabled = false
            layoutManager = llm
            adapter = adapterCurrency
            setHasFixedSize(true)
        }
        currencyErrorState.setOnClickListener {
            viewModel.loadRates()
        }
    }
}
