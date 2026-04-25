package br.dev.allan.controlefinanceiro.domain.usecase

import br.dev.allan.controlefinanceiro.data.remote.CreditCardRemoteDataSource
import br.dev.allan.controlefinanceiro.data.remote.TransactionRemoteDataSource
import br.dev.allan.controlefinanceiro.data.remote.mapper.toDomain
import br.dev.allan.controlefinanceiro.domain.repository.CreditCardRepository
import br.dev.allan.controlefinanceiro.domain.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SyncDataUseCase @Inject constructor(
    private val transactionRemoteDataSource: TransactionRemoteDataSource,
    private val creditCardRemoteDataSource: CreditCardRemoteDataSource,
    private val transactionRepository: TransactionRepository,
    private val creditCardRepository: CreditCardRepository
) {
    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        val transactionsDef = async { transactionRemoteDataSource.fetchAllTransactions() }
        val cardsDef = async { creditCardRemoteDataSource.fetchAllCards() }

        val remoteTransactions = transactionsDef.await().map { it.toDomain() }
        val remoteCards = cardsDef.await().map { it.toDomain() }

        // Salva no banco local (Room) - o onConflictStrategy.REPLACE cuidará da desduplicação
        remoteCards.forEach { creditCardRepository.addCard(it) }
        remoteTransactions.forEach { transactionRepository.insertTransaction(it) }
    }
}
