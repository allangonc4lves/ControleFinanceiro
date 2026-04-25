package br.dev.allan.controlefinanceiro.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.dev.allan.controlefinanceiro.data.dataStore.SettingsManager
import br.dev.allan.controlefinanceiro.domain.usecase.ObserveRemoteDataUseCase
import br.dev.allan.controlefinanceiro.domain.usecase.SyncLocalToRemoteUseCase
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val settingsManager: SettingsManager,
    private val syncLocalToRemoteUseCase: SyncLocalToRemoteUseCase,
    private val observeRemoteDataUseCase: ObserveRemoteDataUseCase,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _userName = MutableStateFlow(auth.currentUser?.displayName ?: "")
    val userName = _userName.asStateFlow()

    private val _userEmail = MutableStateFlow(auth.currentUser?.email ?: "")
    val userEmail = _userEmail.asStateFlow()

    private val _userPhotoUrl = MutableStateFlow(auth.currentUser?.photoUrl?.toString())
    val userPhotoUrl = _userPhotoUrl.asStateFlow()

    init {
        // Atualiza as infos sempre que o estado da auth mudar ou na inicialização
        auth.addAuthStateListener { firebaseAuth ->
            _userName.value = firebaseAuth.currentUser?.displayName ?: ""
            _userEmail.value = firebaseAuth.currentUser?.email ?: ""
            _userPhotoUrl.value = firebaseAuth.currentUser?.photoUrl?.toString()
        }
        
        if (auth.currentUser != null) {
            startSync()
        }
    }

    private fun startSync() {
        viewModelScope.launch {
            try {
                // Sincroniza o que está local -> remoto primeiro
                syncLocalToRemoteUseCase()
                
                // Começa a observar mudanças no Firestore para atualizar o local em tempo real
                observeRemoteDataUseCase()
            } catch (e: Exception) {
                // Falha silenciosa
            }
        }
    }

    val currencyCode = settingsManager.currencyCode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "BRL")

    fun updateCurrency(code: String) {
        viewModelScope.launch {
            settingsManager.setCurrencyCode(code)
        }
    }
}
