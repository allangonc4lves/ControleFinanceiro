package br.dev.allan.controlefinanceiro.data.remote.mapper

import br.dev.allan.controlefinanceiro.data.remote.model.TransactionDto
import br.dev.allan.controlefinanceiro.domain.model.Transaction
import br.dev.allan.controlefinanceiro.utils.constants.TransactionCategory
import br.dev.allan.controlefinanceiro.utils.constants.TransactionDirection
import br.dev.allan.controlefinanceiro.utils.constants.TransactionType

fun Transaction.toDto(userId: String): TransactionDto {
    return TransactionDto(
        id = this.id,
        groupId = this.groupId,
        title = this.title,
        amount = this.amount,
        date = this.date,
        category = this.category.name,
        type = this.type.name,
        isInstallment = this.isInstallment,
        installmentCount = this.installmentCount,
        currentInstallment = this.currentInstallment,
        isPaid = this.isPaid,
        direction = this.direction.name,
        creditCardId = this.creditCardId,
        userId = userId
    )
}

fun TransactionDto.toDomain(): Transaction {
    return Transaction(
        id = this.id,
        groupId = this.groupId,
        title = this.title,
        amount = this.amount,
        date = this.date,
        category = TransactionCategory.valueOf(this.category),
        type = TransactionType.valueOf(this.type),
        isInstallment = this.isInstallment,
        installmentCount = this.installmentCount,
        currentInstallment = this.currentInstallment,
        isPaid = this.isPaid,
        direction = TransactionDirection.valueOf(this.direction),
        creditCardId = this.creditCardId
    )
}
