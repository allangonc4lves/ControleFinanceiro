package br.dev.allan.controlefinanceiro.presentation.ui.screens.creditCardsScreen.components

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import br.dev.allan.controlefinanceiro.domain.model.InputModeCustomTextField
import br.dev.allan.controlefinanceiro.presentation.ui.components.CreditCardPreview
import br.dev.allan.controlefinanceiro.presentation.ui.components.CustomOutlinedTextField
import br.dev.allan.controlefinanceiro.presentation.ui.components.Loading
import br.dev.allan.controlefinanceiro.presentation.ui.components.ZenoDialog
import br.dev.allan.controlefinanceiro.presentation.ui.features.add_transaction.SaveTransactionUiEvent
import br.dev.allan.controlefinanceiro.presentation.ui.screens.creditCardsScreen.CardsViewModel

@Composable
fun AddCreditCardDialog(
    onDismiss: () -> Unit,
    viewModel: CardsViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is SaveTransactionUiEvent.SaveSuccess -> {
                    onDismiss()
                }
            }
        }
    }
    ZenoDialog(
        title ="Novo cartão",
        onDismiss = { onDismiss() },
        onConfirm = { viewModel.saveCard() },
        content = {
            Box(contentAlignment = Alignment.Center) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(if (state.isLoading) 0.5f else 1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CustomOutlinedTextField(
                        value = state.bankName,
                        label = "Banco*",
                        capitalization = KeyboardCapitalization.Sentences,
                        isError = state.bankNameError != null,
                        errorMessage = state.bankNameError ?: "",
                        onValueChange = { viewModel.onBankNameChange(it) }
                    )
                    Row(modifier = Modifier.fillMaxWidth()){
                        CustomOutlinedTextField(
                            modifier = Modifier.weight(0.6f),
                            value = state.brand,
                            label = "Bandeira*",
                            capitalization = KeyboardCapitalization.Sentences,
                            isError = state.brandError != null,
                            errorMessage = state.brandError ?: "",
                            onValueChange = { viewModel.onBrandChange(it) }
                        )
                        CustomOutlinedTextField(
                            modifier = Modifier.weight(0.4f),
                            value = state.lastDigits,
                            label = "Últimos digitos",
                            inputMode = InputModeCustomTextField.DIGITS,
                            maxLength = 4,
                            capitalization = KeyboardCapitalization.None,
                            keyboardType = KeyboardType.NumberPassword,
                            isError = state.lastDigitsError != null,
                            errorMessage = state.lastDigitsError ?: "",
                            onValueChange = {
                               viewModel.onLastDigitsChange(it)
                            }
                        )
                    }

                    Spacer(Modifier.height(4.dp))
                    Text("Selecione uma cor:")
                    ColorCarousel(
                        palette = state.palette,
                        initialSelectedColor = state.backgroundColor,
                        onColorSelected = { viewModel.onColorSelected(it) }
                    )
                    Spacer(Modifier.height(4.dp))
                    CreditCardPreview(
                        bankName = state.bankName,
                        brand = state.brand,
                        lastDigits = state.lastDigits,
                        backgroundColorLong = state.backgroundColor,
                        modifier = Modifier
                            .width(250.dp)
                            .height(160.dp)
                    )
                }

                if (state.isLoading) {
                    Loading()
                }
            }
        }
    )
}