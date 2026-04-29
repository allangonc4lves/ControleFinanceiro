package br.dev.allan.controlefinanceiro.data.remote.model

data class CreditCardDto(
    val id: String = "",
    val bankName: String = "",
    val brand: String = "",
    val lastDigits: Any? = 0,
    val invoiceClosing: Any? = 1,
    val dueDate: Any? = 10,
    val backgroundColor: Any? = 0xFF000000,
    @field:JvmField
    val activated: Boolean = true,
    val userId: String = ""
)
