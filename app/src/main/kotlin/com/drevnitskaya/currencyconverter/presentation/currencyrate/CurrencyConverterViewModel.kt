package com.drevnitskaya.currencyconverter.presentation.currencyrate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.drevnitskaya.currencyconverter.data.entities.CurrencyConversionItem
import com.drevnitskaya.currencyconverter.data.entities.DEFAULT_BASE_AMOUNT
import com.drevnitskaya.currencyconverter.data.entities.ErrorHolder
import com.drevnitskaya.currencyconverter.data.source.local.DEFAULT_BASE_CURRENCY_CODE
import com.drevnitskaya.currencyconverter.domain.FetchRatesUseCase
import com.drevnitskaya.currencyconverter.extensions.addTo
import com.drevnitskaya.currencyconverter.presentation.currencyrate.adapter.BASE_CURRENCY_POSITION
import com.drevnitskaya.currencyconverter.framework.NetworkStateProvider
import com.drevnitskaya.currencyconverter.utils.SingleLiveEvent
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.*
import java.util.concurrent.TimeUnit

private const val PERIOD_RATES_UPDATING_MS = 1000L
private const val DEBOUNCE_TIMEOUT_MS = 300L
private const val BASE_CURRENCY_RATE = 1.0

class CurrencyRateViewModel(
    private val networkStateProvider: NetworkStateProvider,
    private val fetchRatesUseCase: FetchRatesUseCase
) : ViewModel() {
    private val showProgress = MutableLiveData<Boolean>()
    fun getShowProgress(): LiveData<Boolean> = showProgress
    private val showErrorState = MutableLiveData<ErrorHolder>()
    fun getShowErrorState(): LiveData<ErrorHolder> = showErrorState
    private val setCalculatedAmounts = MutableLiveData<List<CurrencyConversionItem>>()
    fun getCalculatedAmounts(): LiveData<List<CurrencyConversionItem>> = setCalculatedAmounts
    private val scrollToBaseCurrency = SingleLiveEvent<Unit>()
    fun getScrollToBaseCurrency(): LiveData<Unit> = scrollToBaseCurrency
    private val showOfflineMode = MutableLiveData<Boolean>()
    fun getShowOfflineMode(): LiveData<Boolean> = showOfflineMode

    private var actualRatesMap = mutableMapOf<String, Double>()
    private var currencyValues = LinkedList<CurrencyConversionItem>()
    private var baseCurrency = CurrencyConversionItem().apply {
        currencyCode = DEFAULT_BASE_CURRENCY_CODE
        amount = DEFAULT_BASE_AMOUNT
        isSelected = true
    }

    private var initialLoading = true
    private var updRatesDisposable: Disposable? = null
    private var disposeBag = CompositeDisposable()
    private var baseAmountInputSubject = PublishSubject.create<String>()
        .apply {
            debounce(DEBOUNCE_TIMEOUT_MS, TimeUnit.MILLISECONDS)
            distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ input ->
                    recalculateAmounts(input)
                }, { /*do nothing*/ })
                .addTo(disposeBag)
        }

    init {
        actualRatesMap[DEFAULT_BASE_CURRENCY_CODE] = BASE_CURRENCY_RATE
        loadRates()
    }

    override fun onCleared() {
        super.onCleared()
        disposeBag.clear()
        updRatesDisposable?.dispose()
    }

    fun loadRates() {
        if (networkStateProvider.isNetworkAvailable()) {
            showErrorState.value = null
            showProgress.value = true
            startRatesUpdating()
        } else {
            showErrorState.value = ErrorHolder.NetworkError()
        }
    }

    fun onCurrencyClicked(selectedPosition: Int) {
        updRatesDisposable?.dispose()

        val newBaseCurrency = currencyValues[selectedPosition]
        newBaseCurrency.isSelected = true
        calculatePrevCurrencyRate(newBaseCurrency)
        this.baseCurrency = newBaseCurrency
        swapCurrencies(selectedPosition, newBaseCurrency)

        updateExistingAmounts()

        setCalculatedAmounts()
        scrollToBaseCurrency.call()

        startRatesUpdating()
    }

    fun onBaseAmountUpdated(input: String) {
        baseAmountInputSubject.onNext(input)
    }

    fun stopRateRefreshing() {
        updRatesDisposable?.dispose()
    }

    fun resumeRateRefreshing() {
        if (showErrorState.value == null && updRatesDisposable?.isDisposed == true) {
            startRatesUpdating()
        }
    }

    private fun calculatePrevCurrencyRate(newBaseCurrency: CurrencyConversionItem) {
        val newBaseRate = actualRatesMap[newBaseCurrency.currencyCode] ?: BASE_CURRENCY_RATE
        val newBaseCurrCode = newBaseCurrency.currencyCode
        actualRatesMap[baseCurrency.currencyCode] = 1 / newBaseRate
        actualRatesMap[newBaseCurrCode] = BASE_CURRENCY_RATE
    }

    private fun swapCurrencies(selectedPosition: Int, newBaseCurrency: CurrencyConversionItem) {
        currencyValues[BASE_CURRENCY_POSITION] = currencyValues[BASE_CURRENCY_POSITION].copy()
            .apply { isSelected = false }
        currencyValues.removeAt(selectedPosition)
        currencyValues.addFirst(newBaseCurrency)
    }

    private fun startRatesUpdating() {
        updRatesDisposable = Observable.interval(PERIOD_RATES_UPDATING_MS, TimeUnit.MILLISECONDS)
            .observeOn(Schedulers.io())
            .filter {
                val isOnline = networkStateProvider.isNetworkAvailable()
                showOfflineMode.postValue(isOnline.not())
                isOnline
            }
            .flatMapSingle { fetchRatesUseCase.execute(currencyCode = baseCurrency.currencyCode) }
            .observeOn(Schedulers.computation())
            .map { rateResponse ->
                rateResponse.rates?.let {
                    actualRatesMap = it as MutableMap<String, Double>
                }
                if (initialLoading) {
                    initialLoading = false
                    calculateInitialAmounts()
                } else {
                    updateExistingAmounts()
                }
            }
            .retry()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                showProgress.value = false
                setCalculatedAmounts()
            }, {
                showProgress.value = true
                showErrorState.value = ErrorHolder.GeneralError()
            })
    }

    private fun calculateInitialAmounts() {
        val toCurrencies = actualRatesMap.map { (k, v) ->
            CurrencyConversionItem(
                currencyCode = k,
                amount = (v * baseCurrency.amount),
                isSelected = baseCurrency.currencyCode == k
            )
        }
        currencyValues.apply {
            add(baseCurrency)
            addAll(toCurrencies)

        }
    }

    private fun updateExistingAmounts() {
        currencyValues.forEach { itemWrapper ->
            val currCode = itemWrapper.currencyCode
            if (actualRatesMap.containsKey(currCode) && currCode != baseCurrency.currencyCode) {
                val amount = (actualRatesMap[currCode] ?: BASE_CURRENCY_RATE) * baseCurrency.amount
                itemWrapper.amount = amount
            }
        }
    }

    private fun recalculateAmounts(input: String) {
        updRatesDisposable?.dispose()
        baseCurrency.amount = if (input.isEmpty()) .0 else input.toDouble()
        updateExistingAmounts()
        setCalculatedAmounts()
        startRatesUpdating()
    }

    private fun setCalculatedAmounts() {
        val tempList = mutableListOf<CurrencyConversionItem>()
        tempList.add(currencyValues[BASE_CURRENCY_POSITION])
        currencyValues.subList(BASE_CURRENCY_POSITION + 1, currencyValues.size).forEach {
            tempList.add(it.copy())
        }
        setCalculatedAmounts.value = tempList
    }
}