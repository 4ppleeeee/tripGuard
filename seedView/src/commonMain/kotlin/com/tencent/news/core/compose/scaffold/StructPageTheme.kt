package com.tencent.news.core.compose.scaffold

import androidx.compose.runtime.Composable
import com.tencent.news.core.page.model.StructPageUiState

@Composable
internal fun StructPageTheme(
    state: StructPageUiState,
    content: @Composable (StructPageUiState) -> Unit,
) {
    content(state)
}