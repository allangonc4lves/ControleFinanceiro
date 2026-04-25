package br.dev.allan.controlefinanceiro.presentation.ui.state

data class CreditCardUiState(
    val bankName: String,
    val brand: String,
    val lastDigits: String,
    val color: Long
)