package br.dev.allan.controlefinanceiro.domain.usecase

import br.dev.allan.controlefinanceiro.domain.model.Transaction
import br.dev.allan.controlefinanceiro.utils.constants.TransactionType
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class GetMonthlyTransactionsUseCase @Inject constructor() {

    operator fun invoke(allTransactions: List<Transaction>, month: YearMonth): List<Transaction> {
        return allTransactions.filter { tx ->
            val txDate = try {
                LocalDate.parse(tx.date)
            } catch (e: Exception) {
                null
            } ?: return@filter false
            
            val txYearMonth = YearMonth.from(txDate)
            
            when {
                // REPEAT: Se for do tipo REPEAT, ela deve aparecer em todos os meses 
                // a partir da data de início (independente de installmentCount ser 0 ou não)
                tx.type == TransactionType.REPEAT -> {
                    !month.isBefore(txYearMonth)
                }

                // Transações expandidas (parcelas individuais já salvas no banco)
                tx.currentInstallment > 0 -> txYearMonth == month
                
                // Parcelamentos legados (registro único que projeta meses)
                tx.isInstallment && tx.installmentCount > 1 -> {
                    val monthsBetween = ChronoUnit.MONTHS.between(txYearMonth.atDay(1), month.atDay(1)).toInt()
                    monthsBetween in 0 until tx.installmentCount
                }
                
                // Transações comuns
                else -> txYearMonth == month
            }
        }
    }

    fun getAmountForMonth(tx: Transaction): Double {
        return if (tx.currentInstallment == 0 && tx.isInstallment && tx.installmentCount > 1 && tx.type != TransactionType.REPEAT) {
            (tx.amount / tx.installmentCount).round2()
        } else {
            tx.amount
        }
    }

    private fun Double.round2() = Math.round(this * 100.0) / 100.0
}