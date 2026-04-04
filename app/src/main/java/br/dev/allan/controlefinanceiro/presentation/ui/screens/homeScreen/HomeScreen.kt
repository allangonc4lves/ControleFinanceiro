package br.dev.allan.controlefinanceiro.presentation.ui.screens.homeScreen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import br.dev.allan.controlefinanceiro.presentation.ui.screens.expenseScreen.ExpenseScreen
import br.dev.allan.controlefinanceiro.presentation.ui.theme.ControleFinanceiroTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ControleFinanceiroTheme {
                ExpenseScreen()
            }
        }
    }
}