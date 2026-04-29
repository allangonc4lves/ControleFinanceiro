package br.dev.allan.controlefinanceiro.domain.usecase

import br.dev.allan.controlefinanceiro.data.remote.CreditCardRemoteDataSource
import br.dev.allan.controlefinanceiro.data.remote.TransactionRemoteDataSource
import br.dev.allan.controlefinanceiro.data.remote.mapper.toDomain
import br.dev.allan.controlefinanceiro.domain.repository.CreditCardRepository
import br.dev.allan.controlefinanceiro.domain.repository.TransactionRepository
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

class ObserveRemoteDataUseCase @Inject constructor(
    private val transactionRemoteDataSource: TransactionRemoteDataSource,
    private val creditCardRemoteDataSource: CreditCardRemoteDataSource,
    private val transactionRepository: TransactionRepository,
    private val creditCardRepository: CreditCardRepository
) {
    suspend operator fun invoke() = coroutineScope {
        // Observa mudanças nas transações
        launch {
            transactionRemoteDataSource.observeTransactions().collectLatest { changes ->
                changes.forEach { (dto, type) ->
                    when (type) {
                        br.dev.allan.controlefinanceiro.data.remote.TransactionRemoteDataSource.DocumentChangeType.UPSERT -> {
                            transactionRepository.insertTransactionsSilent(listOf(dto.toDomain()))
                        }
                        br.dev.allan.controlefinanceiro.data.remote.TransactionRemoteDataSource.DocumentChangeType.DELETE -> {
                            transactionRepository.deleteTransactionSilent(dto.id)
                        }
                    }
                }
            }
        }

        // Observa mudanças nos cartões
        launch {
            creditCardRemoteDataSource.observeCards().collectLatest { dtos ->
                val remoteCards = dtos.map { it.toDomain() }
                if (remoteCards.isNotEmpty()) {
                    creditCardRepository.addCardsSilent(remoteCards)
                }
            }
        }
    }
}
