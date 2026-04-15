package br.dev.allan.controlefinanceiro.util

import br.dev.allan.controlefinanceiro.domain.model.Transaction

fun Transaction.getFormattedDisplay(currencyManager: CurrencyManager, code: String): String {
    //val prefix = if (type == TransactionDirection.EXPENSE) "- " else "+ "
    return currencyManager.formatByCurrencyCode(amount, code)
}