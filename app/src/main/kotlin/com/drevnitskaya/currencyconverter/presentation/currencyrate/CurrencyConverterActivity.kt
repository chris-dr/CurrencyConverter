package com.drevnitskaya.currencyconverter.presentation.currencyrate

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.transition.TransitionManager
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.*
import com.drevnitskaya.currencyconverter.R
import com.drevnitskaya.currencyconverter.extensions.hideSoftKeyboard
import com.drevnitskaya.currencyconverter.presentation.currencyrate.adapter.BASE_CURRENCY_POSITION
import com.drevnitskaya.currencyconverter.presentation.currencyrate.adapter.CurrencyConversionAdapter
import kotlinx.android.synthetic.main.activity_currency_converter.*
import org.koin.android.viewmodel.ext.android.viewModel

class CurrencyConverterActivity : AppCompatActivity() {
    private val viewModel: CurrencyRateViewModel by viewModel()
    private lateinit var adapterCurrency: CurrencyConversionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_currency_converter)
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

    private fun initViewModel() = with(viewModel) {
        showProgress.observe(this@CurrencyConverterActivity, Observer { shouldShow ->
            currencyProgress.visibility = if (shouldShow) View.VISIBLE else View.GONE
        })
        showErrorState.observe(this@CurrencyConverterActivity, Observer { error ->
            currencyErrorState.visibility = if (error != null) {
                currencyErrorState.setText(error.errorMsgResId)
                TransitionManager.beginDelayedTransition(currencyRoot)
                View.VISIBLE
            } else {
                View.GONE
            }
        })
        setCalculatedAmounts.observe(this@CurrencyConverterActivity, Observer { rates ->
            if (currencyRecyclerView.visibility == View.GONE) {
                TransitionManager.beginDelayedTransition(currencyRoot)
                currencyRecyclerView.visibility = View.VISIBLE
            }
            adapterCurrency.items = rates

        })
        scrollToBaseCurrency.observe(this@CurrencyConverterActivity, Observer {
            currencyRecyclerView.scrollToPosition(BASE_CURRENCY_POSITION)
        })
        showOfflineMode.observe(this@CurrencyConverterActivity, Observer { shouldShow ->
            TransitionManager.beginDelayedTransition(currencyRoot)
            currencyOfflineMode.visibility = if (shouldShow) {
                View.VISIBLE
            } else {
                View.GONE
            }
        })
    }

    private fun initViews() {
        adapterCurrency = CurrencyConversionAdapter(onCurrencyClicked = { item ->
            viewModel.onCurrencyClicked(item)
        }, onBaseAmountUpdated = { input ->
            viewModel.onBaseAmountUpdated(input)
        }, onActionDoneClicked = {
            hideSoftKeyboard()
        })
        currencyRecyclerView.apply {
            val llm = LinearLayoutManager(
                this@CurrencyConverterActivity,
                LinearLayoutManager.VERTICAL, false
            )
            layoutManager = llm
            adapter = adapterCurrency
            setHasFixedSize(true)
            addItemDecoration(DividerItemDecoration(this@CurrencyConverterActivity, VERTICAL))
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    when (newState) {
                        SCROLL_STATE_DRAGGING -> viewModel.stopRateRefreshing()
                        SCROLL_STATE_IDLE -> viewModel.resumeRateRefreshing()
                    }
                }
            })
        }
        currencyErrorState.setOnClickListener {
            viewModel.loadRates()
        }
    }
}
