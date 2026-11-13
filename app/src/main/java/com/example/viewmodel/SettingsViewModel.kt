package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.StorePreferences
import com.example.domain.UiResult
import com.example.repository.StoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: StoreRepository
) : ViewModel() {

    val preferences: StateFlow<StorePreferences> = repository.preferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = StorePreferences()
        )

    private val _actionResult = MutableStateFlow<UiResult>(UiResult.Idle)
    val actionResult: StateFlow<UiResult> = _actionResult.asStateFlow()

    fun saveStoreIdentity(storeName: String, businessInfo: String) {
        if (storeName.isBlank()) {
            _actionResult.value = UiResult.Error("Informe o nome da loja.")
            return
        }
        viewModelScope.launch {
            runCatching {
                repository.updateStoreIdentity(storeName, businessInfo)
            }.onSuccess {
                _actionResult.value = UiResult.Success("Dados da loja salvos com sucesso.")
            }.onFailure { err ->
                _actionResult.value = UiResult.Error(err.message ?: "Erro ao salvar dados.")
            }
        }
    }

    fun savePreferences(
        defaultMinStock: Int,
        showLowStockAlerts: Boolean,
        confirmBeforeSale: Boolean
    ) {
        if (defaultMinStock < 1) {
            _actionResult.value = UiResult.Error("O estoque mínimo deve ser pelo menos 1.")
            return
        }
        viewModelScope.launch {
            runCatching {
                repository.updateStorePreferences(defaultMinStock, showLowStockAlerts, confirmBeforeSale)
            }.onSuccess {
                _actionResult.value = UiResult.Success("Preferências atualizadas.")
            }.onFailure { err ->
                _actionResult.value = UiResult.Error(err.message ?: "Erro ao salvar preferências.")
            }
        }
    }

    suspend fun buildBackupJson(): Result<String> {
        return runCatching { repository.exportBackupJson() }
    }

    fun restoreFromBackupJson(json: String) {
        viewModelScope.launch {
            runCatching {
                repository.restoreBackupJson(json)
            }.onSuccess {
                _actionResult.value = UiResult.Success("Backup restaurado com sucesso!")
            }.onFailure { err ->
                _actionResult.value = UiResult.Error(
                    "Falha ao restaurar backup: ${err.message ?: "arquivo inválido"}."
                )
            }
        }
    }

    fun notifyMessage(result: UiResult) {
        _actionResult.value = result
    }

    fun clearResult() {
        _actionResult.value = UiResult.Idle
    }
}
