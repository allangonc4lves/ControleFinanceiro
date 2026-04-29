package br.dev.allan.controlefinanceiro.domain.usecase

import android.util.Log
import br.dev.allan.controlefinanceiro.data.remote.CreditCardRemoteDataSource
import br.dev.allan.controlefinanceiro.data.remote.TransactionRemoteDataSource
import br.dev.allan.controlefinanceiro.data.remote.mapper.toDomain
import br.dev.allan.controlefinanceiro.domain.repository.CreditCardRepository
import br.dev.allan.controlefinanceiro.domain.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SyncDataUseCase @Inject constructor(
    private val transactionRemoteDataSource: TransactionRemoteDataSource,
    private val creditCardRemoteDataSource: CreditCardRemoteDataSource,
    private val transactionRepository: TransactionRepository,
    private val creditCardRepository: CreditCardRepository
) {
    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        try {
            Log.d("SyncDebug", "Iniciando busca de dados remotos...")
            val transactionsDef = async { transactionRemoteDataSource.fetchAllTransactions() }
            val cardsDef = async { creditCardRemoteDataSource.fetchAllCards() }

            val remoteCardsDto = cardsDef.await()
            val remoteTransactionsDto = transactionsDef.await()

            Log.d("SyncDebug", "Dados recebidos: ${remoteCardsDto.size} cartões, ${remoteTransactionsDto.size} transações.")

            val remoteCards = remoteCardsDto.map { it.toDomain() }
            val remoteTransactions = remoteTransactionsDto.map { it.toDomain() }

            if (remoteCards.isNotEmpty()) {
                Log.d("SyncDebug", "Inserindo ${remoteCards.size} cartões no banco local.")
                creditCardRepository.addCardsSilent(remoteCards)
            }
            if (remoteTransactions.isNotEmpty()) {
                Log.d("SyncDebug", "Inserindo ${remoteTransactions.size} transações no banco local.")
                transactionRepository.insertTransactionsSilent(remoteTransactions)
            }
            Log.d("SyncDebug", "Sincronização concluída com sucesso.")
        } catch (e: Exception) {
            Log.e("SyncDebug", "Erro fatal na sincronização de dados: ${e.message}", e)
            throw e
        }
    }
}
