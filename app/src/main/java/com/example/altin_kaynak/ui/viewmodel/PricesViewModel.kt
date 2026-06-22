package com.example.altin_kaynak.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.altin_kaynak.data.api.NetworkModule
import com.example.altin_kaynak.data.model.GoldPrice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class PriceSource { HAREM, ALTINKAYNAK }

sealed class PricesState {
    object Loading : PricesState()
    data class Success(val prices: List<GoldPrice>) : PricesState()
    data class Error(val message: String) : PricesState()
}

class PricesViewModel : ViewModel() {
    private val haremService = NetworkModule.haremApiService
    private val altinkaynakService = NetworkModule.altinkaynakService

    private val _state = MutableStateFlow<PricesState>(PricesState.Loading)
    val state: StateFlow<PricesState> = _state

    private val _selectedSource = MutableStateFlow(PriceSource.HAREM)
    val selectedSource: StateFlow<PriceSource> = _selectedSource

    init {
        loadPrices(PriceSource.HAREM)
    }

    fun selectSource(source: PriceSource) {
        _selectedSource.value = source
        loadPrices(source)
    }

    fun refresh() = loadPrices(_selectedSource.value)

    private fun loadPrices(source: PriceSource) {
        viewModelScope.launch {
            _state.value = PricesState.Loading
            try {
                val prices = when (source) {
                    PriceSource.HAREM -> fetchHaremPrices()
                    PriceSource.ALTINKAYNAK -> altinkaynakService.getPrices()
                }
                _state.value = PricesState.Success(prices)
            } catch (e: Exception) {
                _state.value = PricesState.Error(e.message ?: "Bilinmeyen hata")
            }
        }
    }

    private suspend fun fetchHaremPrices(): List<GoldPrice> {
        val response = haremService.getAllPrices(NetworkModule.HAREM_API_KEY)
        val wantedSymbols = setOf(
            "ALTIN", "XAUUSD", "GUMTRY",
            "USDTRY", "EURTRY", "GBPTRY", "CHFTRY", "JPYTRY"
        )
        return response.data
            .filter { it.symbol in wantedSymbols }
            .map { item ->
                GoldPrice(
                    symbol = item.symbol,
                    name = symbolToName(item.symbol),
                    buy = item.bid,
                    sell = item.ask
                )
            }
    }

    private fun symbolToName(symbol: String): String = when (symbol) {
        "ALTIN"  -> "Gram Altın (₺)"
        "XAUUSD" -> "Ons Altın (USD)"
        "GUMTRY" -> "Gümüş (₺)"
        "USDTRY" -> "Dolar (USD/TRY)"
        "EURTRY" -> "Euro (EUR/TRY)"
        "GBPTRY" -> "Sterlin (GBP/TRY)"
        "CHFTRY" -> "İsviçre Frangı (CHF/TRY)"
        "JPYTRY" -> "Japon Yeni (JPY/TRY)"
        else     -> symbol
    }
}
