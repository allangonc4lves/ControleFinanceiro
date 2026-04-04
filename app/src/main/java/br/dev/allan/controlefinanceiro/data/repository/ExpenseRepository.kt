package br.dev.allan.controlefinanceiro.data.repository

import br.dev.allan.controlefinanceiro.data.local.Expense
import br.dev.allan.controlefinanceiro.data.local.ExpenseDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ExpenseRepository @Inject constructor(private val dao: ExpenseDao) {
    fun getExpenses(): Flow<List<Expense>> = dao.getAllExpenses()
    suspend fun addExpense(expense: Expense) = dao.insertExpense(expense)
    suspend fun updateExpense(expense: Expense) = dao.updateExpense(expense)
    suspend fun deleteExpense(expense: Expense) = dao.deleteExpense(expense)
}