package com.example.domain

enum class Audience(val label: String) {
    MASCULINO("Masculino"),
    FEMININO("Feminino"),
    UNISSEX("Unissex");

    companion object {
        fun fromString(value: String): Audience =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: UNISSEX
    }
}

enum class PaymentMethod(val label: String) {
    DINHEIRO("Dinheiro"),
    PIX("Pix"),
    DEBITO("Cartão de Débito"),
    CREDITO("Cartão de Crédito"),
    OUTROS("Outros");

    companion object {
        fun fromString(value: String): PaymentMethod =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: DINHEIRO
    }
}

enum class OrderStatus(val label: String) {
    PENDENTE("Pendente"),
    EM_ANDAMENTO("Em andamento"),
    CONCLUIDO("Concluído"),
    CANCELADO("Cancelado");

    companion object {
        fun fromString(value: String): OrderStatus =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: PENDENTE
    }
}

enum class MovementType(val label: String) {
    ENTRADA("Entrada"),
    SAIDA_VENDA("Saída (Venda)"),
    AJUSTE("Ajuste"),
    ESTORNO("Estorno");

    companion object {
        fun fromString(value: String): MovementType =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: AJUSTE
    }
}

enum class PeriodFilter(val label: String) {
    HOJE("Hoje"),
    ULTIMOS_7_DIAS("7 dias"),
    ESTE_MES("Este mês"),
    MES_ANTERIOR("Mês anterior"),
    PERSONALIZADO("Personalizado")
}

data class VariantInput(
    val id: Long = 0L,
    val color: String,
    val size: String,
    val quantity: Int
)

data class CartLineItem(
    val productId: Long,
    val variantId: Long,
    val productName: String,
    val productCode: String,
    val color: String,
    val size: String,
    val quantity: Int,
    val availableStock: Int,
    val unitSalePriceCents: Long,
    val unitCostPriceCents: Long
) {
    val lineTotalCents: Long get() = unitSalePriceCents * quantity
    val lineCostCents: Long get() = unitCostPriceCents * quantity
}

data class OrderItemDraft(
    val productId: Long? = null,
    val variantId: Long? = null,
    val productName: String,
    val color: String,
    val size: String,
    val quantity: Int,
    val unitPriceCents: Long,
    val unitCostCents: Long = 0L
) {
    val lineTotalCents: Long get() = unitPriceCents * quantity
}

data class DailyChartPoint(
    val label: String,
    val valueCents: Long
)

data class TopSellingProduct(
    val productId: Long,
    val productName: String,
    val productCode: String,
    val totalQuantity: Int,
    val totalRevenueCents: Long
)

data class FinancialSummary(
    val salesRevenueCents: Long = 0L,
    val orderAdvancesCents: Long = 0L,
    val totalInflowsCents: Long = 0L,
    val cogsCents: Long = 0L,
    val grossProfitCents: Long = 0L,
    val expensesCents: Long = 0L,
    val netBalanceCents: Long = 0L,
    val salesCount: Int = 0,
    val expensesCount: Int = 0
)

data class StorePreferences(
    val storeName: String = "Gestão Store",
    val businessInfo: String = "Moda Masculina & Feminina",
    val defaultMinStock: Int = 3,
    val showLowStockAlerts: Boolean = true,
    val confirmBeforeCompletingSale: Boolean = true
)

sealed interface UiResult {
    data object Idle : UiResult
    data class Success(val message: String) : UiResult
    data class Error(val message: String) : UiResult
}
