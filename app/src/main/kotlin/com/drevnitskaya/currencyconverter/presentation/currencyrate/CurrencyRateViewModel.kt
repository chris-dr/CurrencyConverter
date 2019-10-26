package com.drevnitskaya.currencyconverter.presentation.currencyrate

import androidx.annotation.StringRes
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.drevnitskaya.currencyconverter.R
import com.drevnitskaya.currencyconverter.data.entities.CurrencyItemWrapper
import com.drevnitskaya.currencyconverter.domain.FetchRateUseCase
import com.drevnitskaya.currencyconverter.extensions.addTo
import com.drevnitskaya.currencyconverter.extensions.round
import com.drevnitskaya.currencyconverter.presentation.currencyrate.adapter.BASE_CURRENCY_POSITION
import com.drevnitskaya.currencyconverter.utils.NetworkStateProvider
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

class ItemMoveWrapper(var fromPosition: Int, var toPosition: Int)
class ItemRangeWrapper(var fromPosition: Int, var count: Int, var newAmounts: List<Double>? = null)
sealed class ErrorHolder(@StringRes val errorMsgResId: Int) {
    class NetworkError : ErrorHolder(R.string.shared_noNetworkError_msg)
    class GeneralError : ErrorHolder(R.string.shared_generalError_msg)
}

class CurrencyRateViewModel(
    private val networkStateProvider: NetworkStateProvider,
    private val fetchRateUseCase: FetchRateUseCase
) : ViewModel() {
    val showProgress = MutableLiveData<Boolean>()
    val showErrorState = MutableLiveData<ErrorHolder>()
    val setCalculatedValues = MutableLiveData<List<CurrencyItemWrapper>>()
    val notifyItemMoved = MutableLiveData<ItemMoveWrapper>()
    val notifyItemRangeUpdated = MutableLiveData<ItemRangeWrapper>()
    val notifyItemRangeAmountUpdated = MutableLiveData<ItemRangeWrapper>()

    private var actualRatesMap = mapOf<String, Double>()
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
                .subscribe({ input -> reCalculateValues(input) }, { /*do nothing*/ })
                .addTo(disposeBag)
        }

    init {
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
            fetchRates()
        } else {
            showErrorState.value = ErrorHolder.NetworkError()
        }
    }

    fun onCurrencyClicked(newBaseCurrency: CurrencyItemWrapper) {
        updRatesDisposable?.dispose()

        newBaseCurrency.isSelected = true
        val selectedCurrPosition = currencyValues.indexOf(newBaseCurrency)
        this.baseCurrency.isSelected = false
        this.baseCurrency = newBaseCurrency

        currencyValues.removeAt(selectedCurrPosition)
        currencyValues.addFirst(newBaseCurrency)

        setCalculatedValues.value = currencyValues
        notifyItemMoved.value = ItemMoveWrapper(
            fromPosition = selectedCurrPosition,
            toPosition = BASE_CURRENCY_POSITION
        )
        notifyItemRangeUpdated.value = ItemRangeWrapper(
            fromPosition = BASE_CURRENCY_POSITION,
            count = 2
        )

        fetchRates()
    }

    fun onValueUpdated(input: String) {
        currInputSubject.onNext(input)
    }

    fun stopRateRefreshing() {
        updRatesDisposable?.dispose()
    }

    fun resumeRateRefreshing() {
        if (updRatesDisposable == null || updRatesDisposable?.isDisposed == true) {
            fetchRates()
        }
    }

    private fun fetchRates() {
        updRatesDisposable = Observable.interval(PERIOD_RATES_UPDATING_MS, TimeUnit.MILLISECONDS)
            .observeOn(Schedulers.io())
            .filter { networkStateProvider.isNetworkAvailable() }
            .flatMapSingle { fetchRateUseCase.execute(currencyCode = baseCurrency.currencyCode) }
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
            .retry()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                showProgress.value = false
                updateAmounts()
            }, {
                showProgress.value = true
                if (currencyValues.isEmpty()) {
                    showErrorState.value = ErrorHolder.GeneralError()
                }
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
            if (actualRatesMap.containsKey(currCode)) {
                val amount = (actualRatesMap[currCode] ?: 1.0) * baseCurrency.amount
                itemWrapper.amount = amount.round()
            }
        }
    }

    private fun reCalculateValues(input: String) {
        updRatesDisposable?.dispose()
        baseCurrency.amount = if (input.isEmpty()) 0.0 else input.toDouble()
        updateExistingValues()

        updateAmounts()

        fetchRates()
    }

    private fun updateAmounts() {
        setCalculatedValues.value = currencyValues
        val newAmounts = currencyValues.subList(1, currencyValues.size).map { it.amount.round() }
        notifyItemRangeAmountUpdated.value =
            ItemRangeWrapper(
                fromPosition = 1,
                count = currencyValues.size,
                newAmounts = newAmounts
            )
    }
}