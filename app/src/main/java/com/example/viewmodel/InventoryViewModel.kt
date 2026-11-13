package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.Audience
import com.example.domain.UiResult
import com.example.domain.VariantInput
import com.example.entities.ProductWithVariants
import com.example.entities.StockMovementEntity
import com.example.repository.StoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class InventoryFilterState(
    val searchQuery: String = "",
    val selectedCategory: String = "Todas",
    val selectedAudience: Audience? = null,
    val selectedColor: String = "Todas",
    val selectedSize: String = "Todos",
    val onlyLowStock: Boolean = false
)

data class InventoryUiState(
    val products: List<ProductWithVariants> = emptyList(),
    val allCategories: List<String> = emptyList(),
    val allColors: List<String> = emptyList(),
    val allSizes: List<String> = emptyList(),
    val filter: InventoryFilterState = InventoryFilterState(),
    val lowStockThreshold: Int = 3,
    val recentMovements: List<StockMovementEntity> = emptyList()
)

class InventoryViewModel(
    private val repository: StoreRepository
) : ViewModel() {

    private val _filter = MutableStateFlow(InventoryFilterState())
    private val _actionResult = MutableStateFlow<UiResult>(UiResult.Idle)
    val actionResult: StateFlow<UiResult> = _actionResult.asStateFlow()

    val uiState: StateFlow<InventoryUiState> = combine(
        repository.observeProducts(),
        repository.preferencesFlow,
        repository.observeRecentMovements(150),
        _filter
    ) { products, prefs, movements, filter ->
        val threshold = prefs.defaultMinStock
        val categories = products.map { it.product.category }.distinct().sorted()
        val colors = products.flatMap { p -> p.variants.map { it.color } }.distinct().sorted()
        val sizes = products.flatMap { p -> p.variants.map { it.size } }.distinct().sorted()

        val q = filter.searchQuery.trim().lowercase()
        val filtered = products.filter { pwv ->
            val matchesQuery = q.isEmpty() ||
                pwv.product.name.lowercase().contains(q) ||
                pwv.product.code.lowercase().contains(q)
            val matchesCategory = filter.selectedCategory == "Todas" ||
                pwv.product.category.equals(filter.selectedCategory, ignoreCase = true)
            val matchesAudience = filter.selectedAudience == null ||
                pwv.product.audience == filter.selectedAudience
            val matchesColor = filter.selectedColor == "Todas" ||
                pwv.variants.any { it.color.equals(filter.selectedColor, ignoreCase = true) }
            val matchesSize = filter.selectedSize == "Todos" ||
                pwv.variants.any { it.size.equals(filter.selectedSize, ignoreCase = true) }
            val matchesLowStock = !filter.onlyLowStock ||
                pwv.variants.any { it.quantity <= threshold }

            matchesQuery && matchesCategory && matchesAudience && matchesColor && matchesSize && matchesLowStock
        }

        InventoryUiState(
            products = filtered,
            allCategories = categories,
            allColors = colors,
            allSizes = sizes,
            filter = filter,
            lowStockThreshold = threshold,
            recentMovements = movements
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = InventoryUiState()
    )

    fun observeProduct(productId: Long): Flow<ProductWithVariants?> = repository.observeProduct(productId)
    fun observeProductMovements(productId: Long): Flow<List<StockMovementEntity>> = repository.observeMovementsByProduct(productId)

    fun updateSearch(query: String) {
        _filter.value = _filter.value.copy(searchQuery = query)
    }

    fun updateCategory(category: String) {
        _filter.value = _filter.value.copy(selectedCategory = category)
    }

    fun updateAudience(audience: Audience?) {
        _filter.value = _filter.value.copy(selectedAudience = audience)
    }

    fun updateColor(color: String) {
        _filter.value = _filter.value.copy(selectedColor = color)
    }

    fun updateSize(size: String) {
        _filter.value = _filter.value.copy(selectedSize = size)
    }

    fun toggleLowStockOnly(enabled: Boolean) {
        _filter.value = _filter.value.copy(onlyLowStock = enabled)
    }

    fun clearFilters() {
        _filter.value = InventoryFilterState()
    }

    fun saveProduct(
        productId: Long?,
        name: String,
        description: String,
        category: String,
        audience: Audience,
        photoUri: String?,
        code: String,
        costPriceCents: Long,
        salePriceCents: Long,
        notes: String,
        variants: List<VariantInput>,
        onSuccess: (Long) -> Unit = {}
    ) {
        viewModelScope.launch {
            runCatching {
                repository.saveProduct(
                    productId = productId,
                    name = name,
                    description = description,
                    category = category,
                    audience = audience,
                    photoUri = photoUri,
                    code = code,
                    costPriceCents = costPriceCents,
                    salePriceCents = salePriceCents,
                    notes = notes,
                    variants = variants
                )
            }.onSuccess { savedId ->
                _actionResult.value = UiResult.Success("Produto salvo com sucesso.")
                onSuccess(savedId)
            }.onFailure { err ->
                _actionResult.value = UiResult.Error(err.message ?: "Erro ao salvar produto.")
            }
        }
    }

    fun adjustStock(productId: Long, productName: String, variantId: Long, newQuantity: Int, reason: String) {
        viewModelScope.launch {
            runCatching {
                repository.adjustVariantStock(productId, productName, variantId, newQuantity, reason)
            }.onSuccess {
                _actionResult.value = UiResult.Success("Estoque atualizado com sucesso.")
            }.onFailure { err ->
                _actionResult.value = UiResult.Error(err.message ?: "Não foi possível atualizar o estoque.")
            }
        }
    }

    fun deleteProduct(productId: Long, onDeleted: () -> Unit = {}) {
        viewModelScope.launch {
            runCatching {
                repository.deleteProduct(productId)
            }.onSuccess {
                _actionResult.value = UiResult.Success("Produto excluído.")
                onDeleted()
            }.onFailure { err ->
                _actionResult.value = UiResult.Error(err.message ?: "Erro ao excluir produto.")
            }
        }
    }

    fun clearResult() {
        _actionResult.value = UiResult.Idle
    }
}
