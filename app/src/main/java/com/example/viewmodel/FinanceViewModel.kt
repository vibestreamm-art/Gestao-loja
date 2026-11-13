package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.FinancialSummary
import com.example.domain.OrderStatus
import com.example.domain.PeriodFilter
import com.example.domain.UiResult
import com.example.entities.ExpenseEntity
import com.example.repository.StoreRepository
import com.example.utils.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FinanceUiState(
    val period: PeriodFilter = PeriodFilter.ESTE_MES,
    val customStartMillis: Long = DateUtils.startOfCurrentMonthMillis(),
    val customEndMillis: Long = System.currentTimeMillis(),
    val summary: FinancialSummary = FinancialSummary(),
    val expenses: List<ExpenseEntity> = emptyList()
)

class FinanceViewModel(
    private val repository: StoreRepository
) : ViewModel() {

    private val _period = MutableStateFlow(PeriodFilter.ESTE_MES)
    private val _customStart = MutableStateFlow(DateUtils.startOfCurrentMonthMillis())
    private val _customEnd = MutableStateFlow(System.currentTimeMillis())
    private val _actionResult = MutableStateFlow<UiResult>(UiResult.Idle)
    val actionResult: StateFlow<UiResult> = _actionResult.asStateFlow()

    val uiState: StateFlow<FinanceUiState> = combine(
        repository.observeSales(),
        repository.observeOrders(),
        repository.observeExpenses(),
        _period,
        combine(_customStart, _customEnd) { s, e -> s to e }
    ) { sales, orders, expenses, period, (customStart, customEnd) ->
        val (start, end) = resolveRange(period, customStart, customEnd)

        val periodSales = sales.filter { it.sale.createdAtMillis in start..end }
        val periodExpenses = expenses.filter { it.dateMillis in start..end }
        val periodOrderAdvances = orders
            .filter { it.order.status != OrderStatus.CANCELADO && it.order.orderDateMillis in start..end }
            .sumOf { it.order.paidCents }

        val salesRevenue = periodSales.sumOf { it.sale.totalCents }
        val cogs = periodSales.sumOf { it.sale.totalCostCents }
        val grossProfit = salesRevenue - cogs
        val totalInflows = salesRevenue + periodOrderAdvances
        val expensesTotal = periodExpenses.sumOf { it.amountCents }
        val netBalance = totalInflows - expensesTotal

        FinanceUiState(
            period = period,
            customStartMillis = customStart,
            customEndMillis = customEnd,
            summary = FinancialSummary(
                salesRevenueCents = salesRevenue,
                orderAdvancesCents = periodOrderAdvances,
                totalInflowsCents = totalInflows,
                cogsCents = cogs,
                grossProfitCents = grossProfit,
                expensesCents = expensesTotal,
                netBalanceCents = netBalance,
                salesCount = periodSales.size,
                expensesCount = periodExpenses.size
            ),
            expenses = periodExpenses
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FinanceUiState()
    )

    fun setPeriod(period: PeriodFilter) {
        _period.value = period
    }

    fun setCustomRange(startMillis: Long, endMillis: Long) {
        _customStart.value = startMillis
        _customEnd.value = DateUtils.endOfDayMillis(endMillis)
        _period.value = PeriodFilter.PERSONALIZADO
    }

    fun saveExpense(
        expenseId: Long?,
        description: String,
        category: String,
        amountCents: Long,
        dateMillis: Long,
        notes: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            runCatching {
                repository.saveExpense(expenseId, description, category, amountCents, dateMillis, notes)
            }.onSuccess {
                _actionResult.value = UiResult.Success("Despesa registrada com sucesso.")
                onSuccess()
            }.onFailure { err ->
                _actionResult.value = UiResult.Error(err.message ?: "Erro ao salvar despesa.")
            }
        }
    }

    fun deleteExpense(expenseId: Long) {
        viewModelScope.launch {
            runCatching {
                repository.deleteExpense(expenseId)
            }.onSuccess {
                _actionResult.value = UiResult.Success("Despesa excluída.")
            }.onFailure { err ->
                _actionResult.value = UiResult.Error(err.message ?: "Erro ao excluir despesa.")
            }
        }
    }

    fun clearResult() {
        _actionResult.value = UiResult.Idle
    }

    companion object {
        fun resolveRange(period: PeriodFilter, customStart: Long, customEnd: Long): Pair<Long, Long> {
            val now = System.currentTimeMillis()
            return when (period) {
                PeriodFilter.HOJE -> DateUtils.startOfTodayMillis(now) to DateUtils.endOfDayMillis(now)
                PeriodFilter.ULTIMOS_7_DIAS -> DateUtils.startOfDaysAgoMillis(6, now) to DateUtils.endOfDayMillis(now)
                PeriodFilter.ESTE_MES -> DateUtils.startOfCurrentMonthMillis(now) to DateUtils.endOfDayMillis(now)
                PeriodFilter.MES_ANTERIOR -> DateUtils.previousMonthRangeMillis(now)
                PeriodFilter.PERSONALIZADO -> minOf(customStart, customEnd) to DateUtils.endOfDayMillis(maxOf(customStart, customEnd))
            }
        }
    }
}
