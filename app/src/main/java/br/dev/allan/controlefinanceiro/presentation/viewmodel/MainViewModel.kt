package br.dev.allan.controlefinanceiro.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.dev.allan.controlefinanceiro.data.dataStore.SettingsManager
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
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _userName = MutableStateFlow(auth.currentUser?.displayName ?: "")
    val userName = _userName.asStateFlow()

    init {
        // Atualiza o nome sempre que o estado da auth mudar ou na inicialização
        auth.addAuthStateListener { firebaseAuth ->
            _userName.value = firebaseAuth.currentUser?.displayName ?: ""
        }
        // Tenta sincronizar dados locais remanescentes ao abrir o app se estiver logado
        if (auth.currentUser != null) {
            viewModelScope.launch {
                try {
                    syncLocalToRemoteUseCase()
                } catch (e: Exception) {
                    // Falha silenciosa na inicialização
                }
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