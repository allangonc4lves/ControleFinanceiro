package br.dev.allan.controlefinanceiro.presentation.ui.screens.creditCardsScreen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import br.dev.allan.controlefinanceiro.presentation.ui.components.CreditCardPreview
import br.dev.allan.controlefinanceiro.presentation.viewmodel.AddCreditCardsViewModel

@Composable
fun CreditCardsScreen(
    viewModel: AddCreditCardsViewModel = hiltViewModel(),
) {
    val cards = viewModel.cards.collectAsState()

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { cards.value.size }
    )

    val cardWidth: Dp = 300.dp
    val cardHeight: Dp = 190.dp

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            pageSize = PageSize.Fixed(cardWidth),
            contentPadding = PaddingValues(horizontal = (LocalConfiguration.current.screenWidthDp.dp - cardWidth) / 2),
            pageSpacing = 16.dp,
            beyondViewportPageCount = 2
        ) { page ->
            val card = cards.value[page]
            val scale = if (pagerState.currentPage == page) 1f else 0.92f

            Box(
                modifier = Modifier
                    .graphicsLayer { scaleX = scale; scaleY = scale }
                    .width(cardWidth)
                    .height(cardHeight)
                    .clickable { card.id },
                contentAlignment = Alignment.Center
            ) {
                CreditCardPreview(
                    bankName = card.bankName,
                    brand = card.brand,
                    lastDigits = card.lastDigits.toString(),
                    backgroundColorLong = card.backgroundColor,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

}





