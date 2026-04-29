package br.dev.allan.controlefinanceiro.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.ListenableWorker.Result
import br.dev.allan.controlefinanceiro.data.local.CreditCardDao
import br.dev.allan.controlefinanceiro.data.local.TransactionDao
import br.dev.allan.controlefinanceiro.data.local.mapper.toDomain
import br.dev.allan.controlefinanceiro.data.remote.CreditCardRemoteDataSource
import br.dev.allan.controlefinanceiro.data.remote.TransactionRemoteDataSource
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class FirestoreSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val transactionDao: TransactionDao,
    private val cardDao: CreditCardDao,
    private val transactionRemoteDataSource: TransactionRemoteDataSource,
    private val cardRemoteDataSource: CreditCardRemoteDataSource
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val entityId = inputData.getString(KEY_ENTITY_ID) ?: return Result.failure()
        val entityType = inputData.getString(KEY_ENTITY_TYPE) ?: return Result.failure()
        val operation = inputData.getString(KEY_OPERATION) ?: return Result.failure()

        return try {
            when (entityType) {
                TYPE_TRANSACTION -> syncTransaction(entityId, operation)
                TYPE_CARD -> syncCard(entityId, operation)
                else -> Result.failure()
            }
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    private suspend fun syncTransaction(id: String, operation: String): Result {
        when (operation) {
            OP_UPSERT -> {
                val transaction = transactionDao.getTransactionById(id)?.toDomain()
                if (transaction != null) {
                    transactionRemoteDataSource.saveTransaction(transaction)
                }
            }
            OP_DELETE -> {
                transactionRemoteDataSource.deleteTransaction(id)
            }
        }
        return Result.success()
    }

    private suspend fun syncCard(id: String, operation: String): Result {
        when (operation) {
            OP_UPSERT -> {
                val card = cardDao.getCardById(id)?.toDomain()
                if (card != null) {
                    cardRemoteDataSource.saveCard(card)
                }
            }
            OP_DELETE -> {
                cardRemoteDataSource.deleteCard(id)
            }
        }
        return Result.success()
    }

    companion object {
        const val KEY_ENTITY_ID = "entity_id"
        const val KEY_ENTITY_TYPE = "entity_type"
        const val KEY_OPERATION = "operation"

        const val TYPE_TRANSACTION = "transaction"
        const val TYPE_CARD = "card"

        const val OP_UPSERT = "upsert"
        const val OP_DELETE = "delete"
    }
}
