package com.openflux.app.terminal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun TermuxTerminalView(
    modifier: Modifier = Modifier,
    presenter: TerminalPresenter? = null
) {
    val p = remember {
        presenter ?: TerminalPresenter(LocalContext.current).apply {
            initializeSession()
        }
    }

    DisposableEffect(Unit) {
        onDispose { p.destroy() }
    }

    AndroidView(
        factory = { p.terminalView },
        modifier = modifier.fillMaxSize().background(Color(0xFF1A1A1A))
    )
}
