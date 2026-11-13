package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.CartLineItem
import com.example.domain.PaymentMethod
import com.example.domain.UiResult
import com.example.entities.ClientEntity
import com.example.entities.ProductEntity
import com.example.entities.ProductVariantEntity
import com.example.entities.ProductWithVariants
import com.example.entities.SaleWithItems
import com.example.repository.StoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SalesUiState(
    val products: List<ProductWithVariants> = emptyList(),
    val clients: List<ClientEntity> = emptyList(),
    val salesHistory: List<SaleWithItems> = emptyList(),
    val cartItems: List<CartLineItem> = emptyList(),
    val selectedClient: ClientEntity? = null,
    val selectedPaymentMethod: PaymentMethod = PaymentMethod.PIX,
    val discountCents: Long = 0L,
    val saleNotes: String = "",
    val confirmBeforeSale: Boolean = true,
    val isSubmitting: Boolean = false
) {
    val subtotalCents: Long get() = cartItems.sumOf { it.lineTotalCents }
    val totalCents: Long get() = (subtotalCents - discountCents).coerceAtLeast(0L)
}

class SalesViewModel(
    private val repository: StoreRepository
) : ViewModel() {

    private val _cartItems = MutableStateFlow<List<CartLineItem>>(emptyList())
    private val _selectedClient = MutableStateFlow<ClientEntity?>(null)
    private val _paymentMethod = MutableStateFlow(PaymentMethod.PIX)
    private val _discountCents = MutableStateFlow(0L)
    private val _notes = MutableStateFlow("")
    private val _isSubmitting = MutableStateFlow(false)

    private val _actionResult = MutableStateFlow<UiResult>(UiResult.Idle)
    val actionResult: StateFlow<UiResult> = _actionResult.asStateFlow()

    private val draftFlow = combine(
        _cartItems,
        _selectedClient,
        _paymentMethod,
        _discountCents,
        _notes
    ) { cart, client, payment, discount, notes ->
        DraftTuple(cart, client, payment, discount, notes)
    }

    val uiState: StateFlow<SalesUiState> = combine(
        repository.observeProducts(),
        repository.observeClients(),
        repository.observeSales(),
        repository.preferencesFlow,
        combine(draftFlow, _isSubmitting) { d, sub -> d to sub }
    ) { products, clients, sales, prefs, (draft, submitting) ->
        SalesUiState(
            products = products,
            clients = clients,
            salesHistory = sales,
            cartItems = draft.cart,
            selectedClient = draft.client,
            selectedPaymentMethod = draft.payment,
            discountCents = draft.discount,
            saleNotes = draft.notes,
            confirmBeforeSale = prefs.confirmBeforeCompletingSale,
            isSubmitting = submitting
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SalesUiState()
    )

    private data class DraftTuple(
        val cart: List<CartLineItem>,
        val client: ClientEntity?,
        val payment: PaymentMethod,
        val discount: Long,
        val notes: String
    )

    fun addItemToCart(product: ProductEntity, variant: ProductVariantEntity, quantityToAdd: Int) {
        if (quantityToAdd <= 0) {
            _actionResult.value = UiResult.Error("Informe uma quantidade válida.")
            return
        }
        val current = _cartItems.value.toMutableList()
        val existingIdx = current.indexOfFirst { it.variantId == variant.id }
        if (existingIdx >= 0) {
            val existing = current[existingIdx]
            val newQty = existing.quantity + quantityToAdd
            if (newQty > variant.quantity) {
                _actionResult.value = UiResult.Error(
                    "Estoque insuficiente (${variant.color}/${variant.size}). Disponível: ${variant.quantity}."
                )
                return
            }
            current[existingIdx] = existing.copy(quantity = newQty, availableStock = variant.quantity)
        } else {
            if (quantityToAdd > variant.quantity) {
                _actionResult.value = UiResult.Error(
                    "Estoque insuficiente (${variant.color}/${variant.size}). Disponível: ${variant.quantity}."
                )
                return
            }
            current.add(
                CartLineItem(
                    productId = product.id,
                    variantId = variant.id,
                    productName = product.name,
                    productCode = product.code,
                    color = variant.color,
                    size = variant.size,
                    quantity = quantityToAdd,
                    availableStock = variant.quantity,
                    unitSalePriceCents = product.salePriceCents,
                    unitCostPriceCents = product.costPriceCents
                )
            )
        }
        _cartItems.value = current
    }

    fun updateCartItemQuantity(variantId: Long, newQuantity: Int) {
        val current = _cartItems.value.toMutableList()
        val idx = current.indexOfFirst { it.variantId == variantId }
        if (idx < 0) return

        if (newQuantity <= 0) {
            current.removeAt(idx)
            _cartItems.value = current
            return
        }

        val item = current[idx]
        if (newQuantity > item.availableStock) {
            _actionResult.value = UiResult.Error("Quantidade excede o estoque disponível (${item.availableStock}).")
            return
        }
        current[idx] = item.copy(quantity = newQuantity)
        _cartItems.value = current
    }

    fun removeCartItem(variantId: Long) {
        _cartItems.value = _cartItems.value.filterNot { it.variantId == variantId }
    }

    fun selectClient(client: ClientEntity?) {
        _selectedClient.value = client
    }

    fun selectPaymentMethod(method: PaymentMethod) {
        _paymentMethod.value = method
    }

    fun updateDiscountCents(cents: Long) {
        _discountCents.value = cents.coerceAtLeast(0L)
    }

    fun updateNotes(notes: String) {
        _notes.value = notes
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _selectedClient.value = null
        _discountCents.value = 0L
        _notes.value = ""
    }

    fun completeSale(onCompleted: (Long) -> Unit = {}) {
        if (_isSubmitting.value) return
        val items = _cartItems.value
        if (items.isEmpty()) {
            _actionResult.value = UiResult.Error("Adicione ao menos um produto à venda.")
            return
        }
        val subtotal = items.sumOf { it.lineTotalCents }
        val discount = _discountCents.value
        if (discount > subtotal) {
            _actionResult.value = UiResult.Error("O desconto não pode ser maior que o subtotal.")
            return
        }

        viewModelScope.launch {
            _isSubmitting.value = true
            runCatching {
                repository.completeSale(
                    items = items,
                    discountCents = discount,
                    paymentMethod = _paymentMethod.value,
                    client = _selectedClient.value,
                    notes = _notes.value
                )
            }.onSuccess { saleId ->
                clearCart()
                _actionResult.value = UiResult.Success("Venda #$saleId registrada e estoque atualizado!")
                onCompleted(saleId)
            }.onFailure { err ->
                _actionResult.value = UiResult.Error(err.message ?: "Erro ao finalizar venda.")
            }
            _isSubmitting.value = false
        }
    }

    fun clearResult() {
        _actionResult.value = UiResult.Idle
    }
}
