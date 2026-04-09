package br.dev.allan.controlefinanceiro.data.local.mapper

import br.dev.allan.controlefinanceiro.data.local.TransactionEntity
import br.dev.allan.controlefinanceiro.domain.model.Transaction

fun TransactionEntity.toDomain(): Transaction {
    return Transaction(
        id = id,
        title = title,
        amount = amount,
        date = date,
        category = category,
        isFixed = isFixed,
        isInstallment = isInstallment,
        installmentCount = installmentCount,
        type = type,
    )
}

fun Transaction.toEntity(): TransactionEntity {
    return TransactionEntity(
        id = id,
        title = title,
        amount = amount,
        date = date,
        category = category,
        isFixed = isFixed,
        isInstallment = isInstallment,
        installmentCount = installmentCount,
        type = type,
    )
}