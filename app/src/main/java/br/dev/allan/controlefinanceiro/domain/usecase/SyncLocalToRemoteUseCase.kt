package br.dev.allan.controlefinanceiro.domain.usecase

import br.dev.allan.controlefinanceiro.data.remote.CreditCardRemoteDataSource
import br.dev.allan.controlefinanceiro.data.remote.TransactionRemoteDataSource
import br.dev.allan.controlefinanceiro.domain.repository.CreditCardRepository
import br.dev.allan.controlefinanceiro.domain.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SyncLocalToRemoteUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val creditCardRepository: CreditCardRepository,
    private val transactionRemoteDataSource: TransactionRemoteDataSource,
    private val creditCardRemoteDataSource: CreditCardRemoteDataSource
) {
    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        // Sincroniza Cartões
        val localCards = creditCardRepository.getCards().first()
        localCards.forEach { card ->
            creditCardRemoteDataSource.saveCard(card)
        }

        // Sincroniza Transações
        val localTransactions = transactionRepository.getTransactions().first()
        if (localTransactions.isNotEmpty()) {
            transactionRemoteDataSource.syncTransactions(localTransactions)
        }
    }
}
