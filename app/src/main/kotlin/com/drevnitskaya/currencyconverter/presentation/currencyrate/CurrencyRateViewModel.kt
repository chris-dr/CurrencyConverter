package com.drevnitskaya.currencyconverter.presentation.currencyrate

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.drevnitskaya.currencyconverter.data.entities.CurrencyItemWrapper
import com.drevnitskaya.currencyconverter.data.entities.ErrorHolder
import com.drevnitskaya.currencyconverter.domain.FetchRateUseCase
import com.drevnitskaya.currencyconverter.extensions.addTo
import com.drevnitskaya.currencyconverter.extensions.round
import com.drevnitskaya.currencyconverter.presentation.currencyrate.adapter.BASE_CURRENCY_POSITION
import com.drevnitskaya.currencyconverter.utils.NetworkStateProvider
import com.drevnitskaya.currencyconverter.utils.SingleLiveEvent
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.*
import java.util.concurrent.TimeUnit

private const val DEFAULT_BASE_CURRENCY_CODE = "EUR"
private const val DEFAULT_BASE_AMOUNT = 100.0
private const val PERIOD_RATES_UPDATING_MS = 1000L
private const val DEBOUNCE_TIMEOUT_MS = 300L
private const val BASE_CURRENCY_RATE = 1.0

class CurrencyRateViewModel(
    private val networkStateProvider: NetworkStateProvider,
    private val fetchRateUseCase: FetchRateUseCase
) : ViewModel() {
    val showProgress = MutableLiveData<Boolean>()
    val showErrorState = MutableLiveData<ErrorHolder>()
    val setCalculatedValues = MutableLiveData<List<CurrencyItemWrapper>>()
    val scrollToBaseCurrency = SingleLiveEvent<Unit>()
    val showOfflineMode = MutableLiveData<Boolean>()

    private var actualRatesMap = mutableMapOf<String, Double>()
    private var currencyValues = LinkedList<CurrencyItemWrapper>()
    private var baseCurrency = CurrencyItemWrapper().apply {
        currencyCode = DEFAULT_BASE_CURRENCY_CODE
        amount = DEFAULT_BASE_AMOUNT
        isSelected = true
    }

    private var initialLoading = true
    private var updRatesDisposable: Disposable? = null
    private var disposeBag = CompositeDisposable()
    private var currInputSubject = PublishSubject.create<String>()
        .apply {
            debounce(DEBOUNCE_TIMEOUT_MS, TimeUnit.MILLISECONDS)
            distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ input ->
                    recalculateValues(input)
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

    fun onCurrencyClicked(selectedCurrPosition: Int) {
        updRatesDisposable?.dispose()

        val newBaseCurrency = currencyValues[selectedCurrPosition]
        newBaseCurrency.isSelected = true

        val newBaseRate = actualRatesMap[newBaseCurrency.currencyCode] ?: BASE_CURRENCY_RATE
        val newBaseCurrCode = newBaseCurrency.currencyCode
        actualRatesMap[baseCurrency.currencyCode] = 1 / newBaseRate
        actualRatesMap[newBaseCurrCode] = BASE_CURRENCY_RATE

        this.baseCurrency = newBaseCurrency

        currencyValues[BASE_CURRENCY_POSITION] = currencyValues[BASE_CURRENCY_POSITION]
            .copy().apply { isSelected = false }
        currencyValues.removeAt(selectedCurrPosition)
        currencyValues.addFirst(newBaseCurrency)

        updateExistingValues()

        updateAmounts()
        scrollToBaseCurrency.call()

        startRatesUpdating()
    }

    fun onValueUpdated(input: String) {
        currInputSubject.onNext(input)
    }

    fun stopRateRefreshing() {
        updRatesDisposable?.dispose()
    }

    fun resumeRateRefreshing() {
        if (showErrorState.value == null) {
            if (updRatesDisposable == null || updRatesDisposable?.isDisposed == true) {
                startRatesUpdating()
            }
        }
    }

    private fun startRatesUpdating() {
        updRatesDisposable = Observable.interval(PERIOD_RATES_UPDATING_MS, TimeUnit.MILLISECONDS)
            .observeOn(Schedulers.io())
            .filter {
                val isOnline = networkStateProvider.isNetworkAvailable()
                showOfflineMode.postValue(isOnline.not())
                isOnline
            }
            .flatMapSingle { fetchRateUseCase.execute(currencyCode = baseCurrency.currencyCode) }
            .observeOn(Schedulers.computation())
            .map { rateResponse ->
                rateResponse.rates?.let {
                    actualRatesMap = it as MutableMap<String, Double>
                }
                if (initialLoading) {
                    initialLoading = false
                    calculateInitialValues()
                } else {
                    updateExistingValues()
                }
            }
            .retry()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                showProgress.value = false
                updateAmounts()
            }, {
                showProgress.value = true
                showErrorState.value = ErrorHolder.GeneralError()
            })
    }

    private fun calculateInitialValues() {
        val toCurrencies = actualRatesMap.map { (k, v) ->
            CurrencyItemWrapper(
                currencyCode = k,
                amount = (v * baseCurrency.amount).round(),
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
            if (actualRatesMap.containsKey(currCode) && currCode != baseCurrency.currencyCode) {
                val amount = (actualRatesMap[currCode] ?: BASE_CURRENCY_RATE) * baseCurrency.amount
                itemWrapper.amount = amount.round()
            }
        }
    }

    private fun recalculateValues(input: String) {
        updRatesDisposable?.dispose()
        baseCurrency.amount = if (input.isEmpty()) .0 else input.toDouble()
        updateExistingValues()

        updateAmounts()

        startRatesUpdating()
    }

    private fun updateAmounts() {
        val tempList = mutableListOf<CurrencyItemWrapper>()
        tempList.add(currencyValues[BASE_CURRENCY_POSITION])
        currencyValues.subList(BASE_CURRENCY_POSITION + 1, currencyValues.size).forEach {
            tempList.add(it.copy())
        }
        setCalculatedValues.value = tempList
    }
}