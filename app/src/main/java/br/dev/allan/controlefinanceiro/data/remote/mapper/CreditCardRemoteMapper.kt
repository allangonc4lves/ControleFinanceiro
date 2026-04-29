package br.dev.allan.controlefinanceiro.data.remote.mapper

import br.dev.allan.controlefinanceiro.data.remote.model.CreditCardDto
import br.dev.allan.controlefinanceiro.domain.model.CreditCard

fun CreditCard.toDto(userId: String): CreditCardDto {
    return CreditCardDto(
        id = this.id,
        bankName = this.bankName,
        brand = this.brand,
        lastDigits = this.lastDigits,
        invoiceClosing = this.invoiceClosing,
        dueDate = this.dueDate,
        backgroundColor = this.backgroundColor,
        activated = this.activated,
        userId = userId
    )
}

fun CreditCardDto.toDomain(): CreditCard {
    return CreditCard(
        id = this.id,
        bankName = this.bankName,
        brand = this.brand,
        lastDigits = when (val value = this.lastDigits) {
            is Number -> value.toInt()
            is String -> value.toIntOrNull() ?: 0
            else -> 0
        },
        invoiceClosing = when (val value = this.invoiceClosing) {
            is Number -> value.toInt()
            is String -> value.toIntOrNull() ?: 1
            else -> 1
        },
        dueDate = when (val value = this.dueDate) {
            is Number -> value.toInt()
            is String -> value.toIntOrNull() ?: 10
            else -> 10
        },
        backgroundColor = when (val value = this.backgroundColor) {
            is Number -> value.toLong()
            is String -> value.toLongOrNull() ?: 0xFF000000
            else -> 0xFF000000
        },
        activated = this.activated
    )
}
