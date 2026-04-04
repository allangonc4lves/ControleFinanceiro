package br.dev.allan.controlefinanceiro.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.dev.allan.controlefinanceiro.data.local.Expense
import br.dev.allan.controlefinanceiro.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel() {

    val expenses = repository.getExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addExpense(title: String, amount: Double, date: Long) {
        viewModelScope.launch {
            repository.addExpense(Expense(title = title, amount = amount, date = date))
        }
    }

    fun updateExpense(expense: Expense) {
        viewModelScope.launch { repository.updateExpense(expense) }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch { repository.deleteExpense(expense) }
    }
}