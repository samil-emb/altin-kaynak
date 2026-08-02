package com.example.altin_kaynak.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.altin_kaynak.data.api.NetworkModule
import com.example.altin_kaynak.data.db.AppDatabase
import com.example.altin_kaynak.data.db.SavingsEntity
import com.example.altin_kaynak.data.model.GoldPrice
import com.example.altin_kaynak.data.model.SAVINGS_TYPES
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SavingsLine(
    val entity: SavingsEntity,
    val value: Double
)

data class OwnerGroup(
    val owner: String,
    val items: List<SavingsLine>,
    val totalValue: Double
)

class SavingsViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).savingsDao()
    private val haremService = NetworkModule.haremApiService
    private val altinkaynakService = NetworkModule.altinkaynakService

    private val savings: StateFlow<List<SavingsEntity>> = dao.getAllSavings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val owners: StateFlow<List<String>> = dao.getOwners()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes: StateFlow<List<String>> = dao.getNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedSource = MutableStateFlow(PriceSource.ALTINKAYNAK)
    val selectedSource: StateFlow<PriceSource> = _selectedSource

    private val _prices = MutableStateFlow<List<GoldPrice>>(emptyList())

    private val _simulatedGramPrice = MutableStateFlow<Double?>(null)
    val simulatedGramPrice: StateFlow<Double?> = _simulatedGramPrice

    val currentGramPrice: StateFlow<Double> = combine(_prices, _selectedSource) { priceList, source ->
        gramPriceOf(priceList, source)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    private val effectivePrices: StateFlow<List<GoldPrice>> = combine(_prices, _simulatedGramPrice, _selectedSource) { priceList, simGram, source ->
        if (simGram == null) return@combine priceList
        val realGram = gramPriceOf(priceList, source)
        if (realGram <= 0.0) return@combine priceList
        val ratio = simGram / realGram
        priceList.map { it.copy(buy = it.buy * ratio, sell = it.sell * ratio) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val groups: StateFlow<List<OwnerGroup>> = combine(savings, effectivePrices, _selectedSource) { list, priceList, source ->
        list.groupBy { it.owner }
            .map { (owner, items) ->
                val lines = items
                    .sortedByDescending { it.createdAt }
                    .map { SavingsLine(it, valueOf(it, source, priceList)) }
                OwnerGroup(owner, lines, lines.sumOf { it.value })
            }
            .sortedBy { it.owner.lowercase() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalValue: StateFlow<Double> = groups
        .map { list -> list.sumOf { it.totalValue } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    init {
        loadPrices(PriceSource.ALTINKAYNAK)
    }

    fun selectSource(source: PriceSource) {
        _selectedSource.value = source
        loadPrices(source)
    }

    fun addSaving(owner: String, type: String, quantity: Double, note: String = "") {
        viewModelScope.launch {
            dao.insert(SavingsEntity(owner = owner.trim(), type = type, quantity = quantity, note = note.trim()))
        }
    }

    fun deleteSaving(item: SavingsEntity) {
        viewModelScope.launch {
            dao.delete(item)
        }
    }

    fun applySimulation(gramPrice: Double) {
        _simulatedGramPrice.value = gramPrice
    }

    fun resetSimulation() {
        _simulatedGramPrice.value = null
    }

    private fun loadPrices(source: PriceSource) {
        viewModelScope.launch {
            _prices.value = try {
                when (source) {
                    PriceSource.ALTINKAYNAK -> altinkaynakService.getPrices()
                    PriceSource.HAREM -> fetchHaremPrices()
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    private suspend fun fetchHaremPrices(): List<GoldPrice> {
        val response = haremService.getAllPrices(NetworkModule.HAREM_API_KEY)
        val wantedSymbols = SAVINGS_TYPES.map { it.haremSymbol }.toSet()
        return response.data
            .filter { it.symbol in wantedSymbols }
            .map { item -> GoldPrice(symbol = item.symbol, name = item.symbol, buy = item.bid, sell = item.ask) }
    }

    private fun valueOf(item: SavingsEntity, source: PriceSource, prices: List<GoldPrice>): Double {
        val info = SAVINGS_TYPES.find { it.label == item.type } ?: return 0.0
        val symbol = if (source == PriceSource.ALTINKAYNAK) info.altinkaynakSymbol else info.haremSymbol
        val price = prices.find { it.symbol == symbol } ?: return 0.0
        return item.quantity * price.buy
    }

    private fun gramPriceOf(prices: List<GoldPrice>, source: PriceSource): Double {
        val info = SAVINGS_TYPES.first { it.label == "Gram Altın" }
        val symbol = if (source == PriceSource.ALTINKAYNAK) info.altinkaynakSymbol else info.haremSymbol
        return prices.find { it.symbol == symbol }?.buy ?: 0.0
    }
}
