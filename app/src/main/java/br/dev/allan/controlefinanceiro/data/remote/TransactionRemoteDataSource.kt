package br.dev.allan.controlefinanceiro.data.remote

import br.dev.allan.controlefinanceiro.data.remote.mapper.toDto
import br.dev.allan.controlefinanceiro.data.remote.model.TransactionDto
import br.dev.allan.controlefinanceiro.domain.model.Transaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class TransactionRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val collectionPath = "transactions"

    fun observeTransactions(): Flow<List<Pair<TransactionDto, DocumentChangeType>>> {
        val userId = auth.currentUser?.uid ?: return kotlinx.coroutines.flow.emptyFlow()
        return firestore.collection("users")
            .document(userId)
            .collection(collectionPath)
            .snapshots()
            .map { snapshot ->
                snapshot.documentChanges.map { change ->
                    val dto = change.document.toObject(TransactionDto::class.java)
                    val type = when (change.type) {
                        com.google.firebase.firestore.DocumentChange.Type.ADDED,
                        com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> DocumentChangeType.UPSERT
                        com.google.firebase.firestore.DocumentChange.Type.REMOVED -> DocumentChangeType.DELETE
                    }
                    Pair(dto, type)
                }
            }
    }

    enum class DocumentChangeType { UPSERT, DELETE }

    suspend fun saveTransaction(transaction: Transaction) {
        val userId = auth.currentUser?.uid ?: return
        val dto = transaction.toDto(userId)
        android.util.Log.d("FirestoreSync", "Agendando sincronização da transação: ${dto.id}")
        firestore.collection("users")
            .document(userId)
            .collection(collectionPath)
            .document(dto.id)
            .set(dto)
            .await()
    }

    suspend fun deleteTransaction(transactionId: String) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users")
            .document(userId)
            .collection(collectionPath)
            .document(transactionId)
            .delete()
            .await()
    }

    suspend fun deleteTransactions(transactionIds: List<String>) {
        val userId = auth.currentUser?.uid ?: return
        val batch = firestore.batch()

        transactionIds.forEach { id ->
            val docRef = firestore.collection("users")
                .document(userId)
                .collection(collectionPath)
                .document(id)
            batch.delete(docRef)
        }

        batch.commit().await()
    }

    suspend fun syncTransactions(transactions: List<Transaction>) {
        val userId = auth.currentUser?.uid ?: return
        val batch = firestore.batch()
        
        transactions.forEach { transaction ->
            val dto = transaction.toDto(userId)
            val docRef = firestore.collection("users")
                .document(userId)
                .collection(collectionPath)
                .document(dto.id)
            batch.set(docRef, dto)
        }
        
        batch.commit().await()
    }

    suspend fun fetchAllTransactions(): List<TransactionDto> {
        val userId = auth.currentUser?.uid ?: run {
            android.util.Log.e("SyncDebug", "fetchAllTransactions: userId é nulo!")
            return emptyList()
        }
        android.util.Log.d("SyncDebug", "fetchAllTransactions: buscando dados para o usuário $userId")
        return firestore.collection("users")
            .document(userId)
            .collection(collectionPath)
            .get()
            .await()
            .toObjects(TransactionDto::class.java)
    }
}
