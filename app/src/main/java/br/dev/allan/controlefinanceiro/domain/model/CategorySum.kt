package br.dev.allan.controlefinanceiro.domain.model

data class CategorySum(
    val category: TransactionCategory,
    val total: Double
)