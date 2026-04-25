package br.dev.allan.controlefinanceiro.data.remote.model

import br.dev.allan.controlefinanceiro.utils.constants.TransactionCategory
import br.dev.allan.controlefinanceiro.utils.constants.TransactionDirection
import br.dev.allan.controlefinanceiro.utils.constants.TransactionType

data class TransactionDto(
    val id: String = "",
    val groupId: String? = null,
    val title: String = "",
    val amount: Double = 0.0,
    val date: String = "",
    val category: String = "",
    val type: String = "",
    @field:JvmField
    val isInstallment: Boolean = false,
    val installmentCount: Int = 0,
    val currentInstallment: Int = 0,
    @field:JvmField
    val isPaid: Boolean = false,
    val direction: String = "",
    val creditCardId: String? = null,
    val userId: String = ""
)
