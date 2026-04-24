package br.dev.allan.controlefinanceiro.presentation.ui.screens.state

data class CreditCardUiModel(
    val bankName: String,
    val brand: String,
    val lastDigits: String,
    val color: Long
)