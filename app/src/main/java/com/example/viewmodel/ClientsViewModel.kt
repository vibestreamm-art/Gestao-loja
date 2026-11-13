package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.UiResult
import com.example.entities.ClientEntity
import com.example.entities.SaleWithItems
import com.example.repository.StoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ClientRowModel(
    val client: ClientEntity,
    val purchasesCount: Int,
    val totalSpentCents: Long
)

data class ClientsUiState(
    val searchQuery: String = "",
    val clients: List<ClientRowModel> = emptyList()
)

class ClientsViewModel(
    private val repository: StoreRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _actionResult = MutableStateFlow<UiResult>(UiResult.Idle)
    val actionResult: StateFlow<UiResult> = _actionResult.asStateFlow()

    val uiState: StateFlow<ClientsUiState> = combine(
        repository.observeClients(),
        repository.observeSales(),
        _searchQuery
    ) { clients, sales, query ->
        val salesByClient = sales.filter { it.sale.clientId != null }.groupBy { it.sale.clientId!! }
        val q = query.trim().lowercase()
        val filtered = clients
            .filter {
                q.isEmpty() ||
                    it.fullName.lowercase().contains(q) ||
                    it.phone.lowercase().contains(q)
            }
            .map { c ->
                val clientSales = salesByClient[c.id].orEmpty()
                ClientRowModel(
                    client = c,
                    purchasesCount = clientSales.size,
                    totalSpentCents = clientSales.sumOf { it.sale.totalCents }
                )
            }
        ClientsUiState(searchQuery = query, clients = filtered)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ClientsUiState()
    )

    fun observeClient(clientId: Long): Flow<ClientEntity?> = repository.observeClient(clientId)
    fun observeClientSales(clientId: Long): Flow<List<SaleWithItems>> = repository.observeSalesByClient(clientId)

    fun updateSearch(query: String) {
        _searchQuery.value = query
    }

    fun saveClient(clientId: Long?, fullName: String, phone: String, notes: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            runCatching {
                repository.saveClient(clientId, fullName, phone, notes)
            }.onSuccess {
                _actionResult.value = UiResult.Success("Cliente salvo com sucesso.")
                onSuccess()
            }.onFailure { err ->
                _actionResult.value = UiResult.Error(err.message ?: "Erro ao salvar cliente.")
            }
        }
    }

    fun deleteClient(clientId: Long, onDeleted: () -> Unit = {}) {
        viewModelScope.launch {
            runCatching {
                repository.deleteClient(clientId)
            }.onSuccess {
                _actionResult.value = UiResult.Success("Cliente excluído.")
                onDeleted()
            }.onFailure { err ->
                _actionResult.value = UiResult.Error(err.message ?: "Erro ao excluir cliente.")
            }
        }
    }

    fun clearResult() {
        _actionResult.value = UiResult.Idle
    }
}
