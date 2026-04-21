package br.dev.allan.controlefinanceiro.presentation.ui.features.detail_transaction

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import br.dev.allan.controlefinanceiro.utils.TransactionUIModel

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import br.dev.allan.controlefinanceiro.utils.constants.TransactionDirection
import br.dev.allan.controlefinanceiro.utils.constants.TransactionType

@Composable
fun EditTransactionDialog(
    transaction: TransactionUIModel,
    totalIncome: Double,
    totalPaidExpenses: Double,
    onDismiss: () -> Unit,
    onConfirm: (TransactionUIModel, Boolean) -> Unit
) {
    var title by remember { mutableStateOf(transaction.title) }
    var amountStr by remember { mutableStateOf(transaction.amount.toString()) }
    var isPaid by remember { mutableStateOf(transaction.isPaid) }
    var editAllInstallments by remember { mutableStateOf(false) }

    val newAmount = amountStr.toDoubleOrNull() ?: 0.0
    val originalAmount = transaction.amount
    
    // Saldo disponível sem contar esta transação (se ela estiver paga)
    val availableWithoutThis = totalIncome - (totalPaidExpenses - if (transaction.isPaid) originalAmount else 0.0)
    val isInsufficientBalance = isPaid && transaction.direction == br.dev.allan.controlefinanceiro.utils.constants.TransactionDirection.EXPENSE && newAmount > availableWithoutThis

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Editar Transação") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Valor") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    isError = isInsufficientBalance
                )

                if (isInsufficientBalance) {
                    Text(
                        text = "Saldo insuficiente para manter esta despesa como paga. Desmarque o pagamento para salvar.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (transaction.direction != TransactionDirection.INCOME) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isPaid = !isPaid }
                    ) {
                        Checkbox(checked = isPaid, onCheckedChange = { isPaid = it })
                        Text("Marcar como pago")
                    }
                }

                if (transaction.isInstallment || transaction.type == TransactionType.REPEAT) {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Opções de Edição",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { editAllInstallments = false }
                    ) {
                        RadioButton(
                            selected = !editAllInstallments,
                            onClick = { editAllInstallments = false }
                        )
                        Text(
                            text = if (transaction.isInstallment) "Apenas esta parcela" else "Apenas este mês",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { editAllInstallments = true }
                    ) {
                        RadioButton(
                            selected = editAllInstallments,
                            onClick = { editAllInstallments = true }
                        )
                        Text(
                            text = if (transaction.isInstallment) "Todas as parcelas" else "Todos os meses",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updatedTransaction = transaction.copy(
                        title = title,
                        amount = newAmount,
                        isPaid = isPaid
                    )
                    onConfirm(updatedTransaction, editAllInstallments)
                },
                enabled = !isInsufficientBalance
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}