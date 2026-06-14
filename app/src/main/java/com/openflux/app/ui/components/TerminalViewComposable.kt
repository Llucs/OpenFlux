package com.openflux.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import com.openflux.app.terminal.TerminalPresenter

@Composable
fun TerminalViewComposable(
    presenter: TerminalPresenter,
    modifier: Modifier = Modifier
) {
    DisposableEffect(Unit) {
        presenter.initializeSession()
        onDispose {
            presenter.destroy()
        }
    }

    AndroidView(
        factory = { presenter.terminalView },
        modifier = modifier,
        update = { view ->
            view.setBackgroundColor(Color.Black.hashCode())
        }
    )
}
