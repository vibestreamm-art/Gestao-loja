package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.DailyChartPoint
import com.example.domain.OrderStatus
import com.example.entities.ProductWithVariants
import com.example.entities.SaleWithItems
import com.example.repository.StoreRepository
import com.example.utils.DateUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class DashboardUiState(
    val storeName: String = "Gestão Store",
    val businessInfo: String = "Moda Masculina & Feminina",
    val totalProducts: Int = 0,
    val totalStockItems: Int = 0,
    val todaySalesCount: Int = 0,
    val todayRevenueCents: Long = 0L,
    val monthRevenueCents: Long = 0L,
    val pendingOrdersCount: Int = 0,
    val lowStockThreshold: Int = 3,
    val lowStockProducts: List<ProductWithVariants> = emptyList(),
    val recentSales: List<SaleWithItems> = emptyList(),
    val last7DaysChart: List<DailyChartPoint> = emptyList(),
    val isLoading: Boolean = true
)

class DashboardViewModel(
    repository: StoreRepository
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.preferencesFlow,
        repository.observeProducts(),
        repository.observeSales(),
        repository.observeOrders()
    ) { prefs, products, sales, orders ->
        val now = System.currentTimeMillis()
        val startOfToday = DateUtils.startOfTodayMillis(now)
        val startOfMonth = DateUtils.startOfCurrentMonthMillis(now)

        val todaySales = sales.filter { it.sale.createdAtMillis >= startOfToday }
        val monthSales = sales.filter { it.sale.createdAtMillis >= startOfMonth }
        val pendingOrders = orders.count {
            it.order.status == OrderStatus.PENDENTE || it.order.status == OrderStatus.EM_ANDAMENTO
        }

        val threshold = prefs.defaultMinStock
        val lowStockList = if (prefs.showLowStockAlerts) {
            products.filter { pwv ->
                pwv.variants.any { it.quantity <= threshold }
            }
        } else {
            emptyList()
        }

        val chartPoints = (6L downTo 0L).map { daysAgo ->
            val (dayStart, label) = DateUtils.dayStartMillisForOffset(daysAgo, now)
            val dayEnd = DateUtils.endOfDayMillis(dayStart)
            val totalDay = sales
                .filter { it.sale.createdAtMillis in dayStart..dayEnd }
                .sumOf { it.sale.totalCents }
            DailyChartPoint(label = label, valueCents = totalDay)
        }

        DashboardUiState(
            storeName = prefs.storeName,
            businessInfo = prefs.businessInfo,
            totalProducts = products.size,
            totalStockItems = products.sumOf { it.totalStock },
            todaySalesCount = todaySales.size,
            todayRevenueCents = todaySales.sumOf { it.sale.totalCents },
            monthRevenueCents = monthSales.sumOf { it.sale.totalCents },
            pendingOrdersCount = pendingOrders,
            lowStockThreshold = threshold,
            lowStockProducts = lowStockList,
            recentSales = sales.take(5),
            last7DaysChart = chartPoints,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState()
    )
}
