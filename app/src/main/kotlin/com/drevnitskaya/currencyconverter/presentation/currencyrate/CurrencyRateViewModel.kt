package com.drevnitskaya.currencyconverter.presentation.currencyrate

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.drevnitskaya.currencyconverter.data.entities.CurrencyItemWrapper
import com.drevnitskaya.currencyconverter.domain.FetchRateUseCase
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit

private const val DEFAULT_CURRENCY_CODE = "EUR"
private const val PERIOD_RATES_UPDATING_MS = 1000L
private const val BASE_CURRENCY_POSITION = 0

class ItemMoveWrapper(var fromPosition: Int, var toPosition: Int)

class CurrencyRateViewModel(
    private val fetchRateUseCase: FetchRateUseCase
) : ViewModel() {
    val updateRates = MutableLiveData<List<CurrencyItemWrapper>>()
    val setRates = MutableLiveData<List<CurrencyItemWrapper>>()
    val notifyItemMoved = MutableLiveData<ItemMoveWrapper>()
    val notifyItemUpdated = MutableLiveData<Int>()

    private var actualRatesMap = mutableMapOf<String, Float>()

    private var currencyValues = LinkedList<CurrencyItemWrapper>()

    private var baseCurrency = CurrencyItemWrapper().apply {
        currencyCode = DEFAULT_CURRENCY_CODE
        amount = 100.0f
        isSelected = true
    }

    private var initialLoading = true
    private var updRatesDisp: Disposable? = null

    init {
        fetchRates()
    }

    override fun onCleared() {
        super.onCleared()
        updRatesDisp?.dispose()
    }

    private fun fetchRates() {
        updRatesDisp = Flowable.interval(PERIOD_RATES_UPDATING_MS, TimeUnit.MILLISECONDS)
            .onBackpressureLatest()
            .observeOn(Schedulers.io())
            .flatMapSingle {
                fetchRateUseCase.execute(currencyCode = baseCurrency.currencyCode)
            }
            .observeOn(Schedulers.computation())
            .map { rateResponse ->
                rateResponse.rates?.let { actualRatesMap = it }
                if (initialLoading) {
                    initialLoading = false
                    calculateInitialValues()
                } else {
                    updateExistingValues()
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                updateRates.value = currencyValues
            }, {
                it.printStackTrace()
            })
    }

    fun onCurrencyClicked(newBaseCurrency: CurrencyItemWrapper) {
        updRatesDisp?.dispose()

        newBaseCurrency.isSelected = true
        val selectedCurrPosition = currencyValues.indexOf(newBaseCurrency)
        this.baseCurrency.isSelected = false
        this.baseCurrency = newBaseCurrency


        currencyValues.removeAt(selectedCurrPosition)
        currencyValues.addFirst(newBaseCurrency)

        setRates.value = currencyValues
        notifyItemMoved.value = ItemMoveWrapper(
            fromPosition = selectedCurrPosition,
            toPosition = BASE_CURRENCY_POSITION
        )

        fetchRates()
    }

    private fun calculateInitialValues() {
        val toCurrencies = actualRatesMap.map { (k, v) ->
            CurrencyItemWrapper(
                currencyCode = k,
                amount = v * baseCurrency.amount,
                isSelected = baseCurrency.currencyCode == k
            )
        }

        currencyValues.apply {
            add(baseCurrency)
            addAll(toCurrencies)

        }
    }

    private fun updateExistingValues() {
        currencyValues.forEach { itemWrapper ->
            val currCode = itemWrapper.currencyCode
            if (actualRatesMap.containsKey(currCode)) {
                itemWrapper.amount = (actualRatesMap[currCode] ?: 1f) * baseCurrency.amount
            }
        }
    }
}