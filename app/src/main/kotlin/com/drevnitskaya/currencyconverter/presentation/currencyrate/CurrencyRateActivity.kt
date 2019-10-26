package com.drevnitskaya.currencyconverter.presentation.currencyrate

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.drevnitskaya.currencyconverter.R
import com.drevnitskaya.currencyconverter.presentation.currencyrate.adapter.CurrencyRateAdapter
import kotlinx.android.synthetic.main.activity_currency_rate.*
import org.koin.android.viewmodel.ext.android.viewModel

class CurrencyRateActivity : AppCompatActivity() {
    private val viewModel: CurrencyRateViewModel by viewModel()
    private val adapterCurrency =
        CurrencyRateAdapter(onCurrencyClicked = { item ->
            viewModel.onCurrencyClicked(item)
        }, onValueUpdated = { input ->
            viewModel.onValueUpdated(input)
        })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_currency_rate)
        initViews()
        initViewModel()
    }

    private fun initViewModel() {
        viewModel.apply {
            setRates.observe(this@CurrencyRateActivity, Observer { rates ->
                adapterCurrency.items = rates
            })
            notifyItemRangeUpdated.observe(this@CurrencyRateActivity, Observer { wrapper ->
                adapterCurrency.notifyItemRangeChanged(wrapper.fromPosition, wrapper.count)
            })
            notifyItemRangeAmountUpdated.observe(this@CurrencyRateActivity, Observer { wrapper ->
                adapterCurrency.notifyItemRangeChanged(
                    wrapper.fromPosition,
                    wrapper.count,
                    wrapper.newAmounts
                )
            })
            notifyItemMoved.observe(this@CurrencyRateActivity, Observer { wrapper ->
                val toPosition = wrapper.toPosition
                testRecyclerView.scrollToPosition(toPosition)
                adapterCurrency.notifyItemMoved(wrapper.fromPosition, wrapper.toPosition)
            })
        }
    }

    private fun initViews() {
        testRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@CurrencyRateActivity)
            adapter = adapterCurrency
        }
    }
}
