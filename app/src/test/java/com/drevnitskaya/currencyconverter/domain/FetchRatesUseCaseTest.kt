package com.drevnitskaya.currencyconverter.domain

import com.drevnitskaya.currencyconverter.data.entities.CurrencyRate
import com.drevnitskaya.currencyconverter.data.repository.CurrencyRepository
import com.drevnitskaya.currencyconverter.testutils.getMockedHttpException
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.After
import org.junit.Before
import org.junit.Test

class FetchRatesUseCaseTest {
    private val currencyRepository: CurrencyRepository = mock()
    private lateinit var useCase: FetchRatesUseCase
    private val mockedRates = LinkedHashMap<String, Double>().apply {
        put("AUD", 1.23)
        put("BGN", 5.67)
    }
    private val mockedCurrencyRateResponse = CurrencyRate(
        currency = "EUR",
        rates = mockedRates
    )

    @Before
    fun setup() {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }

        useCase = FetchRatesUseCaseImpl(currencyRepository)
    }

    @After
    fun tearDown() {
        RxAndroidPlugins.reset()
        RxJavaPlugins.reset()
    }

    @Test
    fun execute_success() {
        whenever(currencyRepository.getCurrencyRates(any()))
            .thenReturn(Single.just(mockedCurrencyRateResponse))

        val testObserver = useCase.execute("EUR").test()

        testObserver.assertNoErrors()
        testObserver.assertValue(mockedCurrencyRateResponse)
    }

    @Test
    fun execute_error() {
        val mockedError = getMockedHttpException(500)
        whenever(currencyRepository.getCurrencyRates(any()))
            .thenReturn(Single.error(mockedError))

        val testObserver = useCase.execute("EUR").test()

        testObserver.assertNoValues()
        testObserver.assertError(mockedError)
    }
}