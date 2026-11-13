package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.OrderItemDraft
import com.example.domain.OrderStatus
import com.example.domain.UiResult
import com.example.entities.ClientEntity
import com.example.entities.OrderWithItems
import com.example.entities.ProductWithVariants
import com.example.repository.StoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class OrdersUiState(
    val orders: List<OrderWithItems> = emptyList(),
    val clients: List<ClientEntity> = emptyList(),
    val products: List<ProductWithVariants> = emptyList(),
    val selectedStatusFilter: OrderStatus? = null,
    val searchQuery: String = ""
)

class OrdersViewModel(
    private val repository: StoreRepository
) : ViewModel() {

    private val _statusFilter = MutableStateFlow<OrderStatus?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _actionResult = MutableStateFlow<UiResult>(UiResult.Idle)
    val actionResult: StateFlow<UiResult> = _actionResult.asStateFlow()

    val uiState: StateFlow<OrdersUiState> = combine(
        repository.observeOrders(),
        repository.observeClients(),
        repository.observeProducts(),
        _statusFilter,
        _searchQuery
    ) { orders, clients, products, statusFilter, query ->
        val q = query.trim().lowercase()
        val filtered = orders.filter { owi ->
            val matchesStatus = statusFilter == null || owi.order.status == statusFilter
            val matchesQuery = q.isEmpty() ||
                owi.order.clientNameSnapshot.lowercase().contains(q) ||
                owi.items.any { it.productName.lowercase().contains(q) }
            matchesStatus && matchesQuery
        }
        OrdersUiState(
            orders = filtered,
            clients = clients,
            products = products,
            selectedStatusFilter = statusFilter,
            searchQuery = query
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = OrdersUiState()
    )

    fun setStatusFilter(status: OrderStatus?) {
        _statusFilter.value = status
    }

    fun updateSearch(query: String) {
        _searchQuery.value = query
    }

    fun saveOrder(
        orderId: Long?,
        clientId: Long?,
        clientName: String,
        clientPhone: String,
        items: List<OrderItemDraft>,
        paidCents: Long,
        status: OrderStatus,
        expectedDeliveryMillis: Long?,
        notes: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            runCatching {
                repository.saveOrder(
                    orderId = orderId,
                    clientId = clientId,
                    clientName = clientName,
                    clientPhone = clientPhone,
                    items = items,
                    paidCents = paidCents,
                    status = status,
                    expectedDeliveryMillis = expectedDeliveryMillis,
                    notes = notes
                )
            }.onSuccess {
                _actionResult.value = UiResult.Success("Pedido salvo com sucesso.")
                onSuccess()
            }.onFailure { err ->
                _actionResult.value = UiResult.Error(err.message ?: "Erro ao salvar pedido.")
            }
        }
    }

    fun updateStatus(orderId: Long, status: OrderStatus) {
        viewModelScope.launch {
            runCatching {
                repository.updateOrderStatus(orderId, status)
            }.onSuccess {
                _actionResult.value = UiResult.Success("Status alterado para ${status.label}.")
            }.onFailure { err ->
                _actionResult.value = UiResult.Error(err.message ?: "Erro ao atualizar status.")
            }
        }
    }

    fun registerPayment(orderId: Long, amountCents: Long) {
        viewModelScope.launch {
            runCatching {
                repository.registerOrderPayment(orderId, amountCents)
            }.onSuccess {
                _actionResult.value = UiResult.Success("Pagamento registrado no pedido.")
            }.onFailure { err ->
                _actionResult.value = UiResult.Error(err.message ?: "Erro ao registrar pagamento.")
            }
        }
    }

    fun deleteOrder(orderId: Long) {
        viewModelScope.launch {
            runCatching {
                repository.deleteOrder(orderId)
            }.onSuccess {
                _actionResult.value = UiResult.Success("Pedido excluído.")
            }.onFailure { err ->
                _actionResult.value = UiResult.Error(err.message ?: "Erro ao excluir pedido.")
            }
        }
    }

    fun clearResult() {
        _actionResult.value = UiResult.Idle
    }
}
