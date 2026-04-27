package br.dev.allan.controlefinanceiro.data.remote

import br.dev.allan.controlefinanceiro.data.remote.mapper.toDto
import br.dev.allan.controlefinanceiro.data.remote.model.CreditCardDto
import br.dev.allan.controlefinanceiro.domain.model.CreditCard
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class CreditCardRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val collectionPath = "credit_cards"

    fun observeCards(): Flow<List<CreditCardDto>> {
        val userId = auth.currentUser?.uid ?: return kotlinx.coroutines.flow.emptyFlow()
        return firestore.collection("users")
            .document(userId)
            .collection(collectionPath)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(CreditCardDto::class.java)
            }
    }

    suspend fun saveCard(card: CreditCard) {
        val userId = auth.currentUser?.uid ?: return
        val dto = card.toDto(userId)
        android.util.Log.d("FirestoreSync", "Tentando salvar cartão: ${dto.id} para o usuário: $userId")
        try {
            firestore.collection("users")
                .document(userId)
                .collection(collectionPath)
                .document(card.id)
                .set(dto)
                .await()
            android.util.Log.d("FirestoreSync", "Cartão salvo com sucesso no Firestore: ${dto.id}")
        } catch (e: Exception) {
            android.util.Log.e("FirestoreSync", "Erro ao salvar cartão no Firestore: ${e.message}", e)
        }
    }

    suspend fun deleteCard(cardId: String) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users")
            .document(userId)
            .collection(collectionPath)
            .document(cardId)
            .delete()
            .await()
    }

    suspend fun fetchAllCards(): List<CreditCardDto> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        return firestore.collection("users")
            .document(userId)
            .collection(collectionPath)
            .get()
            .await()
            .toObjects(CreditCardDto::class.java)
    }
}
