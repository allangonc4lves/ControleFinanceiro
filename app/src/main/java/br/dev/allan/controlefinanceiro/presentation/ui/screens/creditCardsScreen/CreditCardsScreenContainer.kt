package br.dev.allan.controlefinanceiro.presentation.ui.screens.creditCardsScreen


import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import br.dev.allan.controlefinanceiro.presentation.viewmodel.CardsViewModel

@Composable
fun CreditCardsScreenContainer(
    viewModel: CardsViewModel = hiltViewModel(),
    onCardClick: (String) -> Unit = {}
) {
    val cardsState = viewModel.cards.collectAsState()

    CreditCardsScreen(
        cards = cardsState.value.sortedBy { it.bankName },
        modifier = Modifier.fillMaxSize(),
        onCardClick = onCardClick
    )
}




