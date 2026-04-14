package br.dev.allan.controlefinanceiro.presentation.ui.features.add_transaction

sealed class SaveTransactionUiEvent {
    object SaveSuccess : SaveTransactionUiEvent()
}