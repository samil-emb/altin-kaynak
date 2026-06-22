package com.example.altin_kaynak.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.altin_kaynak.data.db.AppDatabase
import com.example.altin_kaynak.data.db.SavingsEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SavingsViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).savingsDao()

    val savings: StateFlow<List<SavingsEntity>> = dao.getAllSavings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addSaving(type: String, quantity: Double, note: String = "") {
        viewModelScope.launch {
            dao.insert(SavingsEntity(type = type, quantity = quantity, note = note))
        }
    }

    fun deleteSaving(item: SavingsEntity) {
        viewModelScope.launch {
            dao.delete(item)
        }
    }
}
