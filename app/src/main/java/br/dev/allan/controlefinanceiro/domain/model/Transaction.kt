package br.dev.allan.controlefinanceiro.domain.model

import br.dev.allan.controlefinanceiro.R

data class Transaction(
    val id: Int = 0,
    val title: String,
    val amount: Double,
    val date: Long,
    val category: String? = null,
    val iconResId: Int? = null
)

enum class TransactionType(val iconResId: Int) {
    // Income
    INCOME(R.drawable.ic_launcher_background),
    SALARY(R.drawable.ic_launcher_background),
    BONUS(R.drawable.ic_launcher_background),
    INVESTMENT(R.drawable.ic_launcher_background),
    GIFT(R.drawable.ic_launcher_background),
    SALE(R.drawable.ic_launcher_background),
    OTHER_INCOME(R.drawable.ic_launcher_background),

    // Expense
    EXPENSE(R.drawable.ic_launcher_background),
    FOOD(R.drawable.ic_launcher_background),
    GROCERIES(R.drawable.ic_launcher_background),
    TRANSPORT(R.drawable.ic_launcher_background),
    HOUSING(R.drawable.ic_launcher_background),
    UTILITIES(R.drawable.ic_launcher_background),
    HEALTH(R.drawable.ic_launcher_background),
    EDUCATION(R.drawable.ic_launcher_background),
    ENTERTAINMENT(R.drawable.ic_launcher_background),
    SHOPPING(R.drawable.ic_launcher_background),
    TRAVEL(R.drawable.ic_launcher_background),
    SUBSCRIPTION(R.drawable.ic_launcher_background),
    TAX(R.drawable.ic_launcher_background),
    INSURANCE(R.drawable.ic_launcher_background),
    DONATION(R.drawable.ic_launcher_background),
    OTHER_EXPENSE(R.drawable.ic_launcher_background)
}
