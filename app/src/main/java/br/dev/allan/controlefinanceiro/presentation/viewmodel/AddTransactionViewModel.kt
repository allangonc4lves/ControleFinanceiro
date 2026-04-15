package br.dev.allan.controlefinanceiro.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.dev.allan.controlefinanceiro.data.settings.SettingsManager
import br.dev.allan.controlefinanceiro.domain.model.Transaction
import br.dev.allan.controlefinanceiro.domain.model.TransactionCategory
import br.dev.allan.controlefinanceiro.domain.model.TransactionDirection
import br.dev.allan.controlefinanceiro.domain.model.TransactionType
import br.dev.allan.controlefinanceiro.domain.repository.TransactionRepository
import br.dev.allan.controlefinanceiro.domain.usecase.ValidateAmount
import br.dev.allan.controlefinanceiro.domain.usecase.ValidateCategory
import br.dev.allan.controlefinanceiro.domain.usecase.ValidateText
import br.dev.allan.controlefinanceiro.presentation.ui.features.add_transaction.AddTransactionUiState
import br.dev.allan.controlefinanceiro.presentation.ui.features.add_transaction.SaveTransactionUiEvent
import br.dev.allan.controlefinanceiro.util.CurrencyManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val settingsManager: SettingsManager,
    private val currencyManager: CurrencyManager,
    private val repository: TransactionRepository,
    private val validateText: ValidateText = ValidateText(),
    private val validateAmount: ValidateAmount = ValidateAmount(),
    private val validateCategory: ValidateCategory = ValidateCategory(),
) : ViewModel() {

    var uiState by mutableStateOf(AddTransactionUiState())
        private set

    private val _uiEvent = Channel<SaveTransactionUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    fun onTitleChange(newTitle: String) {
        uiState = uiState.copy(title = newTitle, titleError = null)

    }

    val currentCurrencyCode = settingsManager.currencyCode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "BRL")

    fun onAmountChange(newAmount: String) {
        val digitsOnly = newAmount.filter { it.isDigit() }

        val code = currentCurrencyCode.value

        val doubleValue = digitsOnly.toDoubleOrNull()?.div(100) ?: 0.0
        val formatted = currencyManager.formatByCurrencyCode(doubleValue, code)

        uiState = uiState.copy(amount = formatted)
    }

    fun updateCurrency(newCode: String) {
        viewModelScope.launch {
            settingsManager.setCurrencyCode(newCode)
        }
    }

    fun onCategoryChange(category: TransactionCategory) {
        uiState = uiState.copy(category = category, categoryError = null)
    }

    fun onInstallmentCountChange(count: Int) {
        val validCount = if (count in 2..360) count else 2
        uiState = uiState.copy(installmentCount = validCount)
    }

    fun onDateChange(millis: Long) {
        uiState = uiState.copy(dateMillis = millis)
    }

    fun onDirectionChange(direction: TransactionDirection) {
        uiState = uiState.copy(direction = direction, category = null)
    }

    fun onTransactionTypeChange(type: TransactionType) {
        uiState = uiState.copy(transactionType = type)
    }

    fun saveTransaction() {
        uiState = uiState.copy(isLoading = true)

        val titleResult = validateText.execute(uiState.title)
        val amountResult = validateAmount.execute(uiState.amount)
        val categoryResult = validateCategory.execute(uiState.category)

        val amountToSave = uiState.amount
            .replace("R$", "")
            .replace(Regex("[\\s.]"), "")
            .replace(",", ".")
            .toDoubleOrNull() ?: 0.0

        val hasError = listOf(titleResult, amountResult, categoryResult).any { !it.successful }

        if (hasError) {
            uiState = uiState.copy(isLoading = false)
            uiState = uiState.copy(
                titleError = titleResult.errorMessage,
                amountError = amountResult.errorMessage,
                categoryError = categoryResult.errorMessage,
            )
            return
        } else {
            val transaction = Transaction(
                title = uiState.title,
                amount = amountToSave,
                date = uiState.dateMillis,
                category = uiState.category!!,
                isFixed = uiState.transactionType == TransactionType.FIXED,
                isInstallment = uiState.transactionType == TransactionType.INSTALLMENT,
                installmentCount = if (uiState.transactionType == TransactionType.INSTALLMENT) uiState.installmentCount else 0,
                direction = uiState.direction
            )
            viewModelScope.launch {
                repository.insertTransaction(transaction)
                delay(2000L)
                _uiEvent.send(SaveTransactionUiEvent.SaveSuccess)
                uiState = uiState.copy(isLoading = false)
            }
        }
    }
}