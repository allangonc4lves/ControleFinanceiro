package br.dev.allan.controlefinanceiro.presentation.ui.screens.creditCardsScreen.components

import android.app.Dialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import br.dev.allan.controlefinanceiro.presentation.ui.components.CreditCardPreview
import br.dev.allan.controlefinanceiro.presentation.ui.screens.creditCardsScreen.CardsViewModel
import kotlinx.coroutines.launch

@Composable
fun AddCreditCardDialog(
    showDialog: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Long) -> Unit,
    viewModel: CardsViewModel = hiltViewModel(),
) {
    var showDialog by remember { mutableStateOf(showDialog) }

    val palette = listOf(
        0xFF1E88E5L, 0xFF43A047L, 0xFFF4511EL,  0xFF000000L, 0xFF00897BL,
        0xFF6A1B9AL, 0xFFFFD700L, 0xFFC0C0C0L, 0xFF757575L, 0xFFFF9800L,
    )

    var bankName by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(palette[5]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo Cartão") },
        text = {
            Column {
                OutlinedTextField(value = bankName, onValueChange = { bankName = it }, label = { Text("Banco") })
                OutlinedTextField(value = brand, onValueChange = { brand = it }, label = { Text("Bandeira") })
                Spacer(Modifier.height(12.dp))
                Text("Selecione a cor:")
                ColorCarousel(
                    palette = palette,
                    initialSelectedColor = selectedColor,
                    onColorSelected = { selectedColor = it }
                )
                Spacer(Modifier.height(12.dp))
                CreditCardPreview(
                    bankName = bankName,
                    brand = brand,
                    backgroundColorLong = selectedColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (bankName.isNotBlank() && brand.isNotBlank()) {
                    onConfirm(bankName, brand, selectedColor)
                }
            }) { Text("Salvar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )

    if (showDialog) {

    }
}



@Composable
fun ColorCarousel(
    palette: List<Long>,
    initialSelectedColor: Long,
    onColorSelected: (Long) -> Unit
) {
    val circleBaseSize = 64.dp
    val pageSpacing = 0.dp
    val pageSize = circleBaseSize + pageSpacing

    val startIndex = palette.indexOf(initialSelectedColor).coerceIn(0, palette.lastIndex)
    val pagerState = rememberPagerState(
        initialPage = startIndex,
        pageCount = { palette.size }
    )

    val coroutineScope = rememberCoroutineScope()

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val containerWidth = maxWidth
        val horizontalPadding = (containerWidth - pageSize) / 2

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            pageSize = PageSize.Fixed(pageSize),
            contentPadding = PaddingValues(horizontal = horizontalPadding),
            pageSpacing = pageSpacing
        ) { page ->
            val color = palette[page]

            val pageOffset = (page - pagerState.currentPage) + pagerState.currentPageOffsetFraction
            val scale = (1f - 0.35f * kotlin.math.abs(pageOffset)).coerceIn(0.7f, 1f)

            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .size(circleBaseSize)
                    .clip(CircleShape)
                    .background(Color(color))
                    .border(
                        width = if (page == pagerState.currentPage) 3.dp else 1.dp,
                        color = Color.White,
                        shape = CircleShape
                    )
                    .clickable {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(page)
                        }
                    }
            )
        }
    }

    LaunchedEffect(startIndex) {
        pagerState.scrollToPage(startIndex)
    }

    LaunchedEffect(pagerState.currentPage) {
        onColorSelected(palette[pagerState.currentPage])
    }
}