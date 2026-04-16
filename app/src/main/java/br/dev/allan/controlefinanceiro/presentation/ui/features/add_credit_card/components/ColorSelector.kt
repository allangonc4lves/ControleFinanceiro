package br.dev.allan.controlefinanceiro.presentation.ui.features.add_credit_card.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun ColorSelector(
    palette: List<Long>,
    initialSelectedColor: Long,
    onColorSelected: (Long) -> Unit
) {
    val circleBaseSize = 64.dp
    val pageSpacing = 0.dp
    val pageSize = circleBaseSize + pageSpacing

    val startIndex = palette.indexOf(initialSelectedColor).coerceIn(0, palette.lastIndex)

    val pagerState = rememberPagerState(
        initialPage = Int.MAX_VALUE / 2 + startIndex,
        pageCount = { Int.MAX_VALUE }
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
            val color = palette[page % palette.size]

            val pageOffset = (page - pagerState.currentPage) + pagerState.currentPageOffsetFraction
            val scale = (1f - 0.35f * abs(pageOffset)).coerceIn(0.7f, 1f)

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

    LaunchedEffect(pagerState.currentPage) {
        val color = palette[pagerState.currentPage % palette.size]
        onColorSelected(color)
    }
}