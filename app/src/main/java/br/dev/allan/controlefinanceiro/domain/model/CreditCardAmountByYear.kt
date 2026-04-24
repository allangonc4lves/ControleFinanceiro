package br.dev.allan.controlefinanceiro.domain.model

data class CreditCardAmountByYear(
    val monthName: String,
    val totalValue: Double,
    val isSelected: Boolean,
    val isPaid: Boolean
)