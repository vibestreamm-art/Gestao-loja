package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.repository.StoreRepository

class StoreViewModelFactory(
    private val repository: StoreRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> DashboardViewModel(repository) as T
            modelClass.isAssignableFrom(InventoryViewModel::class.java) -> InventoryViewModel(repository) as T
            modelClass.isAssignableFrom(SalesViewModel::class.java) -> SalesViewModel(repository) as T
            modelClass.isAssignableFrom(ClientsViewModel::class.java) -> ClientsViewModel(repository) as T
            modelClass.isAssignableFrom(OrdersViewModel::class.java) -> OrdersViewModel(repository) as T
            modelClass.isAssignableFrom(FinanceViewModel::class.java) -> FinanceViewModel(repository) as T
            modelClass.isAssignableFrom(ReportsViewModel::class.java) -> ReportsViewModel(repository) as T
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> SettingsViewModel(repository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
