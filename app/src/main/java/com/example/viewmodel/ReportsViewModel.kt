package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CsvExporter
import com.example.domain.FinancialSummary
import com.example.domain.OrderStatus
import com.example.domain.PeriodFilter
import com.example.domain.TopSellingProduct
import com.example.entities.ProductWithVariants
import com.example.entities.SaleWithItems
import com.example.entities.StockMovementEntity
import com.example.repository.StoreRepository
import com.example.utils.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ReportsUiState(
    val period: PeriodFilter = PeriodFilter.ESTE_MES,
    val customStartMillis: Long = DateUtils.startOfCurrentMonthMillis(),
    val customEndMillis: Long = System.currentTimeMillis(),
    val salesInPeriod: List<SaleWithItems> = emptyList(),
    val topProducts: List<TopSellingProduct> = emptyList(),
    val lowStockProducts: List<Pair<ProductWithVariants, Int>> = emptyList(),
    val movementsInPeriod: List<StockMovementEntity> = emptyList(),
    val financialSummary: FinancialSummary = FinancialSummary(),
    val minStockThreshold: Int = 3
)

class ReportsViewModel(
    repository: StoreRepository
) : ViewModel() {

    private val _period = MutableStateFlow(PeriodFilter.ESTE_MES)
    private val _customStart = MutableStateFlow(DateUtils.startOfCurrentMonthMillis())
    private val _customEnd = MutableStateFlow(System.currentTimeMillis())

    private val baseDataFlow = combine(
        repository.observeSales(),
        repository.observeProducts(),
        repository.observeOrders(),
        repository.observeExpenses(),
        repository.observeRecentMovements(300)
    ) { sales, products, orders, expenses, movements ->
        BaseTuple(sales, products, orders, expenses, movements)
    }

    val uiState: StateFlow<ReportsUiState> = combine(
        baseDataFlow,
        repository.preferencesFlow,
        _period,
        combine(_customStart, _customEnd) { s, e -> s to e }
    ) { base, prefs, period, (customStart, customEnd) ->
        val (start, end) = FinanceViewModel.resolveRange(period, customStart, customEnd)
        val periodSales = base.sales.filter { it.sale.createdAtMillis in start..end }
        val periodExpenses = base.expenses.filter { it.dateMillis in start..end }
        val periodMovements = base.movements.filter { it.createdAtMillis in start..end }
        val orderAdvances = base.orders
            .filter { it.order.status != OrderStatus.CANCELADO && it.order.orderDateMillis in start..end }
            .sumOf { it.order.paidCents }

        val topProducts = periodSales
            .flatMap { it.items }
            .groupBy { it.productId }
            .map { (productId, items) ->
                TopSellingProduct(
                    productId = productId,
                    productName = items.first().productNameSnapshot,
                    productCode = items.first().productCodeSnapshot,
                    totalQuantity = items.sumOf { it.quantity },
                    totalRevenueCents = items.sumOf { it.lineTotalCents }
                )
            }
            .sortedByDescending { it.totalQuantity }

        val threshold = prefs.defaultMinStock
        val lowStock = base.products
            .filter { pwv -> pwv.variants.any { it.quantity <= threshold } }
            .map { it to threshold }

        val salesRevenue = periodSales.sumOf { it.sale.totalCents }
        val cogs = periodSales.sumOf { it.sale.totalCostCents }
        val expensesTotal = periodExpenses.sumOf { it.amountCents }
        val totalInflows = salesRevenue + orderAdvances

        ReportsUiState(
            period = period,
            customStartMillis = customStart,
            customEndMillis = customEnd,
            salesInPeriod = periodSales,
            topProducts = topProducts,
            lowStockProducts = lowStock,
            movementsInPeriod = periodMovements,
            financialSummary = FinancialSummary(
                salesRevenueCents = salesRevenue,
                orderAdvancesCents = orderAdvances,
                totalInflowsCents = totalInflows,
                cogsCents = cogs,
                grossProfitCents = salesRevenue - cogs,
                expensesCents = expensesTotal,
                netBalanceCents = totalInflows - expensesTotal,
                salesCount = periodSales.size,
                expensesCount = periodExpenses.size
            ),
            minStockThreshold = threshold
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ReportsUiState()
    )

    private data class BaseTuple(
        val sales: List<SaleWithItems>,
        val products: List<ProductWithVariants>,
        val orders: List<com.example.entities.OrderWithItems>,
        val expenses: List<com.example.entities.ExpenseEntity>,
        val movements: List<StockMovementEntity>
    )

    fun setPeriod(period: PeriodFilter) {
        _period.value = period
    }

    fun setCustomRange(startMillis: Long, endMillis: Long) {
        _customStart.value = startMillis
        _customEnd.value = DateUtils.endOfDayMillis(endMillis)
        _period.value = PeriodFilter.PERSONALIZADO
    }

    fun generateCsvContent(): String {
        val state = uiState.value
        val periodDesc = if (state.period == PeriodFilter.PERSONALIZADO) {
            "${DateUtils.formatDate(state.customStartMillis)} a ${DateUtils.formatDate(state.customEndMillis)}"
        } else {
            state.period.label
        }
        return CsvExporter.exportSalesReport(
            periodLabel = periodDesc,
            sales = state.salesInPeriod,
            topProducts = state.topProducts,
            lowStockProducts = state.lowStockProducts,
            movements = state.movementsInPeriod,
            financial = state.financialSummary
        )
    }
}
