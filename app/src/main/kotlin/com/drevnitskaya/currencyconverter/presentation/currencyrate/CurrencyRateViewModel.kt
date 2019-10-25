package com.drevnitskaya.currencyconverter.presentation.currencyrate

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.drevnitskaya.currencyconverter.domain.FetchRateUseCase
import com.drevnitskaya.currencyconverter.extensions.addTo
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

private const val DEFAULT_CURRENCY_CODE = "EUR"

class CurrencyRateViewModel(
    private val fetchRateUseCase: FetchRateUseCase
) : ViewModel() {
    val testLiveData = MutableLiveData<Boolean>()
    private val compositeDisposable = CompositeDisposable()

    init {
        fetchRate()
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }

    private fun fetchRate() {
        Observable.interval(1000, TimeUnit.MILLISECONDS)
            .flatMapSingle { fetchRateUseCase.execute(currencyCode = DEFAULT_CURRENCY_CODE) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ rate ->
                rate?.rates?.forEach { (k, v) ->
                    println("Currency: $k, rate: $v")
                }
            }, {
                it.printStackTrace()
                //TODO: Show error state
            })
            .addTo(compositeDisposable)
    }
}