package br.dev.allan.controlefinanceiro.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import br.dev.allan.controlefinanceiro.domain.model.Transaction
import br.dev.allan.controlefinanceiro.domain.model.TransactionINorEX
import br.dev.allan.controlefinanceiro.presentation.ui.state.AddTransactionINorEX
import br.dev.allan.controlefinanceiro.presentation.ui.state.AddTransactionType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    onDismiss: () -> Unit,
    onConfirm: (Transaction) -> Unit
) {
    // Estados básicos
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var instinstallmentCount by remember { mutableStateOf("") }
    var checkTransactionType by remember { mutableStateOf(AddTransactionType.DEFAULT) }
    var selectedIncomeOrExpense by remember { mutableStateOf(0) }

    // Configuração do DatePicker
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    var showDatePicker by remember { mutableStateOf(false) }

    // Configuração do Dropdown de Categorias
    val categories = listOf("Salario","Alimentação", "Cartão de Crédito", "Transporte", "Lazer", "Saúde", "Outros")
    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(categories[0]) }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("OK") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { TextTitle("Cadastro de transação", MaterialTheme.colorScheme.primary) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Título e Valor (campos anteriores...)
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Valor") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                // --- SELETOR DE DATA ---
                OutlinedTextField(
                    value = datePickerState.selectedDateMillis?.let {
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it))
                    } ?: "",
                    onValueChange = {},
                    label = { Text("Data") },
                    readOnly = true, // Evita abrir o teclado
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Selecionar Data")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                TextTitle("Tipo de transação", MaterialTheme.colorScheme.primary)

                CustomSingleChoiceSegmentedButton(
                    selectedIncomeOrExpense = selectedIncomeOrExpense,
                    onSelectionChange = {
                        selectedIncomeOrExpense = it
                    }
                )

                //Verifica se está marcado como fixo
                CustomSwitch(
                    text = "Fixa",
                    checked = checkTransactionType == AddTransactionType.FIXED,
                    onCheckedChange = { isChecked ->
                        // Se marcar este, automaticamente o outro desmarca porque o estado muda
                        checkTransactionType =
                            if (isChecked) AddTransactionType.FIXED else AddTransactionType.DEFAULT
                    }
                )

                if(selectedIncomeOrExpense == 1){
                    CustomSwitch(
                        text = "Parcelado",
                        checked = checkTransactionType == AddTransactionType.INSTALLMENT,
                        onCheckedChange = { isChecked ->
                            checkTransactionType =
                                if (isChecked) AddTransactionType.INSTALLMENT else AddTransactionType.DEFAULT
                        }
                    )

                    if (checkTransactionType == AddTransactionType.INSTALLMENT) {
                        OutlinedTextField(
                            value = instinstallmentCount,
                            onValueChange = { instinstallmentCount = it },
                            label = { Text("Quantidade de parcelas") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // --- DROPDOWN DE CATEGORIA ---
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoria") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    selectedCategory = category
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        Transaction(
                            title = title,
                            amount = amount.toDoubleOrNull() ?: 0.0,
                            date = datePickerState.selectedDateMillis ?: System.currentTimeMillis(),
                            category = selectedCategory,
                            type = TransactionINorEX.entries[checkTransactionType.ordinal],
                            isFixed = checkTransactionType == AddTransactionType.FIXED,
                            isInstallment = checkTransactionType == AddTransactionType.INSTALLMENT,
                            installmentCount = instinstallmentCount.toIntOrNull() ?: 0
                        )
                    )
                }
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
