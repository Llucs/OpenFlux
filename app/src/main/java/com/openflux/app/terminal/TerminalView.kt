package com.openflux.app.terminal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun TermuxTerminalView(
    modifier: Modifier = Modifier,
    presenter: TerminalPresenter? = null
) {
    val p = remember {
        presenter ?: TerminalPresenter(androidx.compose.ui.platform.LocalContext.current).apply {
            initializeSession()
        }
    }

    AndroidView(
        factory = { p.terminalView },
        modifier = modifier.fillMaxSize().background(Color(0xFF1A1A1A))
    )
}
