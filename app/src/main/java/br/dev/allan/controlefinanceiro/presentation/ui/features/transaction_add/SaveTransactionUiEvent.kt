package br.dev.allan.controlefinanceiro.presentation.ui.features.transaction_add

sealed class SaveTransactionUiEvent {
    object SaveSuccess : SaveTransactionUiEvent()
}