package br.dev.allan.controlefinanceiro.presentation.viewmodel

import android.text.format.DateFormat
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.dev.allan.controlefinanceiro.data.dataStore.SettingsManager
import br.dev.allan.controlefinanceiro.data.local.PaymentStatusEntity
import br.dev.allan.controlefinanceiro.domain.model.CreditCard
import br.dev.allan.controlefinanceiro.domain.model.Transaction
import br.dev.allan.controlefinanceiro.utils.constants.TransactionDirection
import br.dev.allan.controlefinanceiro.utils.TransactionUIModel
import br.dev.allan.controlefinanceiro.domain.repository.CreditCardRepository
import br.dev.allan.controlefinanceiro.domain.repository.TransactionRepository
import br.dev.allan.controlefinanceiro.utils.CurrencyManager
import br.dev.allan.controlefinanceiro.utils.DateHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

enum class TransactionTypeFilter { ALL, INCOME, EXPENSE, INVOICES_ONLY, WALLET_ONLY }
enum class TransactionStatusFilter { ALL, PAID, UNPAID }

data class ReportFilterState(
    val startDate: Long,
    val endDate: Long,
    val typeFilter: TransactionTypeFilter = TransactionTypeFilter.ALL,
    val statusFilter: TransactionStatusFilter = TransactionStatusFilter.ALL,
    val categoryFilter: String? = null
)

data class ReportUIState(
    val items: List<ReportItem> = emptyList(),
    val formattedTotalIncome: String = "",
    val formattedTotalExpense: String = "",
    val formattedBalance: String = "",
    val isLoading: Boolean = false
)

sealed class ReportItem {
    abstract val dateForSorting: Long

    data class Transaction(
        val model: TransactionUIModel,
        override val dateForSorting: Long
    ) : ReportItem()

    data class Invoice(
        val cardId: String,
        val cardName: String,
        val monthYear: String,
        val totalAmount: Double,
        val formattedAmount: String,
        val isPaid: Boolean,
        val transactions: List<TransactionUIModel>,
        override val dateForSorting: Long
    ) : ReportItem()
}

fun Double.round2() = Math.round(this * 100.0) / 100.0

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val cardRepository: CreditCardRepository,
    private val settingsManager: SettingsManager,
    private val currencyManager: CurrencyManager
) : ViewModel() {

    private val _filterState = MutableStateFlow(getDefaultMonthRange())
    val filterState = _filterState.asStateFlow()

    private val currencyCode = settingsManager.currencyCode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "BRL")

    val isBalanceVisible = settingsManager.isBalanceVisible
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    @OptIn(ExperimentalCoroutinesApi::class)
    val reportUiState = combine(
        _filterState,
        currencyCode,
        transactionRepository.getAllPaymentStatuses(),
        cardRepository.getCards()
    ) { filters, code, payments, cards ->
        transactionRepository.getTransactions().map { allTransactions ->
            processReportData(allTransactions, filters, code, payments, cards)
        }
    }.flatMapLatest { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportUIState(isLoading = true))

    private fun processReportData(
        allTransactions: List<Transaction>,
        filters: ReportFilterState,
        code: String,
        payments: List<PaymentStatusEntity>,
        cards: List<CreditCard>
    ): ReportUIState {
        val reportItems = mutableListOf<ReportItem>()
        val creditCardGroups = mutableMapOf<String, Pair<Long, MutableList<TransactionUIModel>>>()

        allTransactions.forEach { tx ->
            if (filters.categoryFilter != null && tx.category.name != filters.categoryFilter) {
                return@forEach
            }

            val occurrences = if (tx.creditCardId != null) {
                val txMillis = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(tx.date)?.time
                if (txMillis != null && txMillis in filters.startDate..filters.endDate) {
                    listOf(txMillis)
                } else {
                    emptyList()
                }
            } else {
                getOccurrencesInRange(tx, filters.startDate, filters.endDate)
            }

            occurrences.forEach { occurrenceDate ->
                val calOcc = Calendar.getInstance().apply { timeInMillis = occurrenceDate }
                val currentMonthYear = SimpleDateFormat("MM-yyyy", Locale.getDefault()).format(calOcc.time)

                val isPaidInThisMonth = if (tx.creditCardId != null) {
                    payments.any { it.transactionId == tx.id.toString() && it.monthYear == currentMonthYear }
                } else {
                    tx.isPaid
                }

                val matchesType = when (filters.typeFilter) {
                    TransactionTypeFilter.ALL -> true
                    TransactionTypeFilter.INCOME -> tx.direction == TransactionDirection.INCOME
                    TransactionTypeFilter.EXPENSE -> tx.direction == TransactionDirection.EXPENSE
                    TransactionTypeFilter.INVOICES_ONLY -> tx.creditCardId != null
                    TransactionTypeFilter.WALLET_ONLY -> tx.creditCardId == null
                }

                val matchesStatus = when (filters.statusFilter) {
                    TransactionStatusFilter.ALL -> true
                    TransactionStatusFilter.PAID -> isPaidInThisMonth
                    TransactionStatusFilter.UNPAID -> !isPaidInThisMonth
                }

                if (matchesType && matchesStatus) {
                    val currentParcel = tx.getCurrentParcelIndex(occurrenceDate)
                    val rawParcel = tx.amount
                    val roundedParcel = Math.round(rawParcel * 100.0) / 100.0

                    val datePattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), "ddMM")
                    val uiModel = TransactionUIModel(
                        id = tx.id,
                        title = tx.title,
                        amount = roundedParcel,
                        formattedTotalAmount = currencyManager.formatByCurrencyCode(tx.amount, code),
                        formattedAmount = currencyManager.formatByCurrencyCode(roundedParcel, code),
                        formattedParcelInfo = if (tx.isInstallment) "$currentParcel/${tx.installmentCount}" else null,
                        dateMillis = occurrenceDate,
                        formattedDate = SimpleDateFormat(datePattern, Locale.getDefault()).format(Date(occurrenceDate)),
                        color = if (tx.direction == TransactionDirection.EXPENSE) Color.Red else Color.Green,
                        category = tx.category,
                        type = tx.type,
                        direction = tx.direction,
                        isPaid = isPaidInThisMonth,
                        isInstallment = tx.isInstallment,
                        currentInstallment = tx.currentInstallment,
                        installmentCount = tx.installmentCount,
                        creditCardId = tx.creditCardId,
                    )

                    if (tx.creditCardId != null) {
                        if (filters.typeFilter != TransactionTypeFilter.WALLET_ONLY) {
                            val key = "${tx.creditCardId}_$currentMonthYear"
                            if (!creditCardGroups.containsKey(key)) creditCardGroups[key] = Pair(occurrenceDate, mutableListOf())
                            creditCardGroups[key]?.second?.add(uiModel)
                        }
                    } else if (filters.typeFilter != TransactionTypeFilter.INVOICES_ONLY) {
                        reportItems.add(ReportItem.Transaction(uiModel, occurrenceDate))
                    }
                }
            }
        }
        creditCardGroups.forEach { (key, groupData) ->
            val dateSort = groupData.first
            val txs = groupData.second
            val total = txs.sumOf { it.amount }
            val cardId = txs.first().creditCardId ?: ""

            val cardInfo = cards.find { it.id == cardId }
            val displayName = cardInfo?.let { "${it.bankName} (${it.brand})" } ?: "Cartão Removido"

            reportItems.add(ReportItem.Invoice(
                cardId = cardId,
                cardName = displayName,
                monthYear = key.split("_")[1],
                totalAmount = total,
                formattedAmount = currencyManager.formatByCurrencyCode(total, code),
                isPaid = txs.all { it.isPaid },
                transactions = txs,
                dateForSorting = dateSort
            ))
        }

        val totalIncome = reportItems
            .filterIsInstance<ReportItem.Transaction>()
            .filter { it.model.direction == TransactionDirection.INCOME }
            .sumOf { it.model.amount }

        val totalExpense = reportItems.sumOf { item ->
            when (item) {
                is ReportItem.Transaction -> {
                    if (item.model.direction == TransactionDirection.EXPENSE) item.model.amount else 0.0
                }
                is ReportItem.Invoice -> item.totalAmount
            }
        }

        return ReportUIState(
            items = reportItems.sortedByDescending { it.dateForSorting },
            formattedTotalIncome = currencyManager.formatByCurrencyCode(totalIncome, code),
            formattedTotalExpense = currencyManager.formatByCurrencyCode(totalExpense, code),
            formattedBalance = currencyManager.formatByCurrencyCode(totalIncome - totalExpense, code),
            isLoading = false
        )
    }

    fun updateTypeFilter(type: TransactionTypeFilter) { _filterState.update { it.copy(typeFilter = type) } }
    fun updateStatusFilter(status: TransactionStatusFilter) { _filterState.update { it.copy(statusFilter = status) } }
    fun updateDateRange(start: Long, end: Long) { _filterState.update { it.copy(startDate = start, endDate = end) } }

    private fun getDefaultMonthRange(): ReportFilterState {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val start = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        cal.add(Calendar.MILLISECOND, -1)
        return ReportFilterState(startDate = start, endDate = cal.timeInMillis)
    }

    private fun getOccurrencesInRange(tx: Transaction, start: Long, end: Long): List<Long> {
        val dates = mutableListOf<Long>()
        val startStr = DateHelper.fromMillisToDb(start)
        val endStr = DateHelper.fromMillisToDb(end)
        val calTx = Calendar.getInstance().apply {
            val dateObj = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(tx.date)
            time = dateObj ?: Date()
        }

        when {
            tx.isInstallment -> {
                for (i in 0 until tx.installmentCount) {
                    val calParcel = (calTx.clone() as Calendar).apply {
                        add(Calendar.MONTH, i)
                    }
                    val parcelMillis = calParcel.timeInMillis
                    if (parcelMillis in start..end) {
                        dates.add(parcelMillis)
                    }
                }
            }
            else -> {
                if (tx.date in startStr..endStr) {
                    val txMillis = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(tx.date)?.time
                    txMillis?.let { dates.add(it) }
                }
            }
        }
        return dates
    }

    fun updateCategoryFilter(categoryName: String?) {
        _filterState.update { it.copy(categoryFilter = categoryName) }
    }
}
