package com.drevnitskaya.currencyconverter.presentation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.drevnitskaya.currencyconverter.data.entities.CurrencyRate
import com.drevnitskaya.currencyconverter.data.entities.ErrorHolder
import com.drevnitskaya.currencyconverter.domain.FetchRatesUseCase
import com.drevnitskaya.currencyconverter.framework.NetworkStateProvider
import com.drevnitskaya.currencyconverter.presentation.currencyrate.CurrencyRateViewModel
import com.drevnitskaya.currencyconverter.testutils.getMockedHttpException
import com.drevnitskaya.currencyconverter.testutils.testObserver
import com.nhaarman.mockitokotlin2.*
import com.nhaarman.mockitokotlin2.verify
import io.reactivex.Single
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.TestScheduler
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.TimeUnit

class CurrencyRateViewModelTest {
    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()
    private val networkStateProvider: NetworkStateProvider = mock()
    private val fetchRatesUseCase: FetchRatesUseCase = mock()
    private val mockedRates = LinkedHashMap<String, Double>().apply {
        put("AUD", 1.23)
        put("BGN", 5.67)
    }
    private val mockedCurrencyRateResponse = CurrencyRate(
        currency = "EUR",
        rates = mockedRates
    )
    private val testScheduler = TestScheduler()
    private lateinit var viewModel: CurrencyRateViewModel


    @Before
    fun setup() {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxJavaPlugins.setComputationSchedulerHandler { testScheduler }
    }

    @After
    fun tearDown() {
        RxAndroidPlugins.reset()
        RxJavaPlugins.reset()
    }

    @Test
    fun init_loadRates_noNetworkError() {
        whenever(networkStateProvider.isNetworkAvailable())
            .thenReturn(false)

        initViewModel()

        val showErrorStateTest = viewModel.getShowErrorState().testObserver()
        val showErrorStateValues = showErrorStateTest.observedValues
        assertEquals(1, showErrorStateValues.size)
        assertTrue(showErrorStateValues[0] is ErrorHolder.NetworkError)
    }

    @Test
    fun init_loadRates_noNetworkConnectionBanner_resumeRatesUpdating() {
        whenever(networkStateProvider.isNetworkAvailable())
            .thenReturn(true, false, true)
        whenever(fetchRatesUseCase.execute(any()))
            .thenReturn(Single.just(mockedCurrencyRateResponse))

        initViewModel()

        val showOfflineModeTest = viewModel.getShowOfflineMode().testObserver()
        val setCalculatedAmountsTest = viewModel.getCalculatedAmounts()
            .testObserver()

        testScheduler.advanceTimeBy(1000, TimeUnit.MILLISECONDS)
        assertEquals(true, showOfflineModeTest.observedValues[0])

        testScheduler.advanceTimeBy(1000, TimeUnit.MILLISECONDS)
        assertEquals(false, showOfflineModeTest.observedValues[1])

        verify(fetchRatesUseCase).execute(any())
        assertEquals(1, setCalculatedAmountsTest.observedValues.size)

        val observedCalculatedAmounts = setCalculatedAmountsTest.observedValues
        val firstActualAmount = observedCalculatedAmounts[0]?.get(0)!!
        assertEquals("EUR", firstActualAmount.currencyCode)
        assertEquals(100.0, firstActualAmount.amount, 0.0)

        val secondActualAmount = observedCalculatedAmounts[0]?.get(1)!!
        assertEquals("AUD", secondActualAmount.currencyCode)
        assertEquals(100.0 * 1.23, secondActualAmount.amount, 0.0)

        val thirdActualAmount = observedCalculatedAmounts[0]?.get(2)!!
        assertEquals("BGN", thirdActualAmount.currencyCode)
        assertEquals(100.0 * 5.67, thirdActualAmount.amount, 0.0)
    }

    @Test
    fun init_loadRates_fetchRatesError_resumeRatesUpdating() {
        whenever(networkStateProvider.isNetworkAvailable())
            .thenReturn(true)
        whenever(fetchRatesUseCase.execute(any()))
            .thenReturn(
                Single.error(getMockedHttpException(500)),
                Single.just(mockedCurrencyRateResponse)
            )

        initViewModel()

        val setCalculatedAmountsTest = viewModel.getCalculatedAmounts()
            .testObserver()

        testScheduler.advanceTimeBy(1000, TimeUnit.MILLISECONDS)
        assertEquals(0, setCalculatedAmountsTest.observedValues.size)

        testScheduler.advanceTimeBy(1000, TimeUnit.MILLISECONDS)
        assertEquals(1, setCalculatedAmountsTest.observedValues.size)

        inOrder(networkStateProvider, fetchRatesUseCase) {
            verify(networkStateProvider, times(2)).isNetworkAvailable()
            verify(fetchRatesUseCase).execute(any())
            verify(networkStateProvider).isNetworkAvailable()
            verify(fetchRatesUseCase).execute(any())
        }
    }

    @Test
    fun init_loadRates_success() {
        whenever(networkStateProvider.isNetworkAvailable())
            .thenReturn(true)
        whenever(fetchRatesUseCase.execute(any()))
            .thenReturn(Single.just(mockedCurrencyRateResponse))

        initViewModel()

        val showProgressTest = viewModel.getShowProgress().testObserver()
        val setCalculatedAmountsTest = viewModel.getCalculatedAmounts()
            .testObserver()
        val showErrorStateTest = viewModel.getShowErrorState().testObserver()

        testScheduler.advanceTimeBy(1000, TimeUnit.MILLISECONDS)
        assertEquals(1, setCalculatedAmountsTest.observedValues.size)

        testScheduler.advanceTimeBy(1000, TimeUnit.MILLISECONDS)
        assertEquals(2, setCalculatedAmountsTest.observedValues.size)

        testScheduler.advanceTimeBy(1000, TimeUnit.MILLISECONDS)
        assertEquals(3, setCalculatedAmountsTest.observedValues.size)

        verify(networkStateProvider, times(4)).isNetworkAvailable()
        assertEquals(true, showProgressTest.observedValues[0])
        assertEquals(false, showProgressTest.observedValues[1])
        assertEquals(null, showErrorStateTest.observedValues[0])
    }

    @Test
    fun onCurrencyClicked() {
        whenever(networkStateProvider.isNetworkAvailable())
            .thenReturn(true)
        whenever(fetchRatesUseCase.execute(any()))
            .thenReturn(Single.just(mockedCurrencyRateResponse))
        initViewModel()
        testScheduler.advanceTimeBy(1000, TimeUnit.MILLISECONDS)

        val setCalculatedAmountsTest = viewModel.getCalculatedAmounts()
            .testObserver()
        val observedAmounts = setCalculatedAmountsTest.observedValues
        val firstBaseAmount = observedAmounts[0]?.get(0)!!

        assertEquals("EUR", firstBaseAmount.currencyCode)
        assertEquals(100.0, firstBaseAmount.amount, 0.0)

        viewModel.onCurrencyClicked(selectedPosition = 1)

        val secondBaseAmount = observedAmounts[1]?.get(0)!!
        assertEquals("AUD", secondBaseAmount.currencyCode)
        assertEquals(100.0 * 1.23, secondBaseAmount.amount, 0.0)
    }

    @Test
    fun onBaseAmountUpdated() {
        whenever(networkStateProvider.isNetworkAvailable())
            .thenReturn(true)
        whenever(fetchRatesUseCase.execute(any()))
            .thenReturn(Single.just(mockedCurrencyRateResponse))
        initViewModel()
        testScheduler.advanceTimeBy(1000, TimeUnit.MILLISECONDS)

        viewModel.onBaseAmountUpdated(input = "150")

        testScheduler.advanceTimeBy(300, TimeUnit.MILLISECONDS)

        val setCalculatedAmountsTest = viewModel.getCalculatedAmounts()
            .testObserver()
        val observedCalculatedAmounts = setCalculatedAmountsTest.observedValues
        val firstActualAmount = observedCalculatedAmounts[0]?.get(0)!!
        assertEquals("EUR", firstActualAmount.currencyCode)
        assertEquals(150.0, firstActualAmount.amount, 0.0)

        val secondActualAmount = observedCalculatedAmounts[0]?.get(1)!!
        assertEquals("AUD", secondActualAmount.currencyCode)
        assertEquals(150.0 * 1.23, secondActualAmount.amount, 0.0)

        val thirdActualAmount = observedCalculatedAmounts[0]?.get(2)!!
        assertEquals("BGN", thirdActualAmount.currencyCode)
        assertEquals(150.0 * 5.67, thirdActualAmount.amount, 0.0)
    }

    @Test
    fun stopRateRefreshing() {
        whenever(networkStateProvider.isNetworkAvailable())
            .thenReturn(true)
        whenever(fetchRatesUseCase.execute(any()))
            .thenReturn(Single.just(mockedCurrencyRateResponse))
        initViewModel()
        testScheduler.advanceTimeBy(1000, TimeUnit.MILLISECONDS)

        viewModel.stopRateRefreshing()

        testScheduler.advanceTimeBy(1000, TimeUnit.MILLISECONDS)

        verify(fetchRatesUseCase).execute(any())
    }

    @Test
    fun resumeRateRefreshing() {
        whenever(networkStateProvider.isNetworkAvailable())
            .thenReturn(true)
        whenever(fetchRatesUseCase.execute(any()))
            .thenReturn(Single.just(mockedCurrencyRateResponse))
        initViewModel()
        testScheduler.advanceTimeBy(1000, TimeUnit.MILLISECONDS)
        viewModel.stopRateRefreshing()

        viewModel.resumeRateRefreshing()

        testScheduler.advanceTimeBy(1000, TimeUnit.MILLISECONDS)
        verify(fetchRatesUseCase, times(2)).execute(any())
    }

    private fun initViewModel() {
        viewModel = CurrencyRateViewModel(networkStateProvider, fetchRatesUseCase)
    }
}