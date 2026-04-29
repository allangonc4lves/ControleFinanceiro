package br.dev.allan.controlefinanceiro.data.repository

import androidx.work.*
import br.dev.allan.controlefinanceiro.data.local.CreditCardDao
import br.dev.allan.controlefinanceiro.data.local.mapper.toDomain
import br.dev.allan.controlefinanceiro.data.local.mapper.toEntity
import br.dev.allan.controlefinanceiro.data.worker.FirestoreSyncWorker
import br.dev.allan.controlefinanceiro.domain.model.CreditCard
import br.dev.allan.controlefinanceiro.domain.repository.CreditCardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreditCardRepositoryImpl @Inject constructor(
    private val dao: CreditCardDao,
    private val workManager: WorkManager
) : CreditCardRepository {

    override fun getCards(): Flow<List<CreditCard>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getCardById(id: String): CreditCard? {
        return dao.getCardById(id)?.toDomain()
    }

    override suspend fun updateCard(card: CreditCard) {
        dao.updateCard(card.toEntity())
        scheduleSync(card.id, FirestoreSyncWorker.OP_UPSERT)
    }

    override suspend fun addCard(card: CreditCard) {
        dao.insert(card.toEntity())
        scheduleSync(card.id, FirestoreSyncWorker.OP_UPSERT)
    }

    override suspend fun addCardsSilent(cards: List<CreditCard>) {
        val entities = cards.map { it.toEntity() }
        entities.forEach { dao.insert(it) }
    }

    override suspend fun removeCard(id: String) {
        dao.deleteById(id)
        scheduleSync(id, FirestoreSyncWorker.OP_DELETE)
    }

    private fun scheduleSync(id: String, operation: String) {
        val data = Data.Builder()
            .putString(FirestoreSyncWorker.KEY_ENTITY_ID, id)
            .putString(FirestoreSyncWorker.KEY_ENTITY_TYPE, FirestoreSyncWorker.TYPE_CARD)
            .putString(FirestoreSyncWorker.KEY_OPERATION, operation)
            .build()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<FirestoreSyncWorker>()
            .setConstraints(constraints)
            .setInputData(data)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, WorkRequest.MIN_BACKOFF_MILLIS, java.util.concurrent.TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniqueWork(
            "sync_card_$id",
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )
    }
}
