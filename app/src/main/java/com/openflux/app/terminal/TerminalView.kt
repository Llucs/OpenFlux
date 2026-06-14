package com.openflux.app.terminal

import android.view.KeyEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.termux.view.TerminalView

@Composable
fun TermuxTerminalView(
    modifier: Modifier = Modifier,
    onInitialized: (TermuxTerminalManager) -> Unit = {}
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A))
    ) {
        val manager = remember {
            val termuxView = TerminalView(context)
            val termManager = TermuxTerminalManager(termuxView)
            onInitialized(termManager)
            termManager
        }

        AndroidView(
            factory = { ctx ->
                val view = TerminalView(ctx)
                manager.let { mgr ->
                    // Re-initialize with this view context
                    val env = arrayOf(
                        "TERM=xterm-256color",
                        "HOME=/data/data/com.termux/files/home",
                        "PREFIX=/data/data/com.termux/files/usr",
                        "PATH=/data/data/com.termux/files/usr/bin:/data/data/com.termux/files/usr/bin/applets:/system/bin:/system/xbin",
                        "LANG=en_US.UTF-8"
                    )
                    val args = arrayOf("-l")
                    val shellPath = "/data/data/com.termux/files/usr/bin/bash"
                    val session = com.termux.terminal.TerminalSession(
                        shellPath, "~", args, env, 2000, null
                    )
                    view.attachSession(session)
                    view.setTerminalViewClient(object : com.termux.view.TerminalViewClient {
                        override fun onScale(scale: Float): Float = scale
                        override fun onSingleTapUp(e: android.view.MotionEvent?) {}
                        override fun shouldBackButtonBeMappedToEscape(): Boolean = false
                        override fun shouldEnforceCharBasedInput(): Boolean = true
                        override fun shouldUseCtrlSpaceWorkaround(): Boolean = false
                        override fun isTerminalViewSelected(): Boolean = true
                        override fun copyModeChanged(copyMode: Boolean) {}
                        override fun onKeyDown(keyCode: Int, e: android.view.KeyEvent?, session: com.termux.terminal.TerminalSession?): Boolean = false
                        override fun onKeyUp(keyCode: Int, e: android.view.KeyEvent?): Boolean = false
                        override fun onLongPress(event: android.view.MotionEvent?): Boolean = false
                        override fun readControlKey(): Boolean = false
                        override fun readAltKey(): Boolean = false
                        override fun readShiftKey(): Boolean = false
                        override fun readFnKey(): Boolean = false
                        override fun onCodePoint(codePoint: Int, ctrlDown: Boolean, session: com.termux.terminal.TerminalSession?): Boolean = false
                        override fun onEmulatorSet() {}
                        override fun logError(tag: String?, message: String?) { android.util.Log.e(tag, message) }
                        override fun logWarn(tag: String?, message: String?) { android.util.Log.w(tag, message) }
                        override fun logInfo(tag: String?, message: String?) { android.util.Log.i(tag, message) }
                        override fun logDebug(tag: String?, message: String?) { android.util.Log.d(tag, message) }
                        override fun logVerbose(tag: String?, message: String?) { android.util.Log.v(tag, message) }
                        override fun logStackTraceWithMessage(tag: String?, message: String?, e: java.lang.Exception?) { android.util.Log.e(tag, message, e) }
                        override fun logStackTrace(tag: String?, e: java.lang.Exception?) { android.util.Log.e(tag, "stacktrace", e) }
                    })
                }
                view
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
