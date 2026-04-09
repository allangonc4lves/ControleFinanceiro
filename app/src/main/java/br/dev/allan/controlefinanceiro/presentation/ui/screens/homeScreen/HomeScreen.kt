package br.dev.allan.controlefinanceiro.presentation.ui.screens.homeScreen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material.Text
import br.dev.allan.controlefinanceiro.presentation.ui.screens.MainScreen
import br.dev.allan.controlefinanceiro.presentation.ui.theme.ControleFinanceiroTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeScreen : ComponentActivity() {
    @Inject
    lateinit var viewModel: testeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ControleFinanceiroTheme {
                MainScreen()
                //Text(viewModel.text)
            }
        }
    }
}

class testeViewModel @Inject constructor() {

    val text = "texto teste"

}

