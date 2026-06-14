package com.openflux.app.terminal

import android.content.Context
import android.view.KeyEvent
import android.view.MotionEvent
import com.termux.terminal.TerminalSession
import com.termux.terminal.TerminalSessionClient
import com.termux.view.TerminalView
import com.termux.view.TerminalViewClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TerminalPresenter(context: Context) : TerminalViewClient, TerminalSessionClient {

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _title = MutableStateFlow("Terminal")
    val title: StateFlow<String> = _title.asStateFlow()

    val terminalView: TerminalView = TerminalView(context, null).apply {
        setTerminalViewClient(this@TerminalPresenter)
    }

    var session: TerminalSession? = null
        private set

    fun initializeSession(shellPath: String = findShell(), cwd: String = "/") {
        session = TerminalSession(
            shellPath, cwd, null, null, 2000, this
        )
        session?.let { s ->
            terminalView.attachSession(s)
            _isRunning.value = true
        }
    }

    fun executeCommand(command: String) {
        session?.write(command + "\n")
    }

    fun destroy() {
        session?.finishIfRunning()
    }

    private fun findShell(): String {
        val termuxShell = "/data/data/com.termux/files/usr/bin/bash"
        return if (java.io.File(termuxShell).exists()) termuxShell
        else "/system/bin/sh"
    }

    override fun onTextChanged(changedSession: TerminalSession) {
        terminalView.onScreenUpdated()
    }

    override fun onTitleChanged(changedSession: TerminalSession) {
        _title.value = changedSession.title ?: "Terminal"
    }

    override fun onSessionFinished(finishedSession: TerminalSession) {
        _isRunning.value = false
    }

    override fun onCopyTextToClipboard(session: TerminalSession, text: String) {
        val clipboard = terminalView.context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        clipboard.setPrimaryClip(android.content.ClipData.newPlainText("terminal", text))
    }

    override fun onPasteTextFromClipboard(session: TerminalSession?) {
        val clipboard = terminalView.context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clipData = clipboard.primaryClip
        if (clipData != null && clipData.itemCount > 0) {
            val text = clipData.getItemAt(0).text
            if (text != null) session?.write(text.toString())
        }
    }

    override fun onBell(session: TerminalSession) {}

    override fun onColorsChanged(session: TerminalSession) {}

    override fun onTerminalCursorStateChange(state: Boolean) {}

    override fun setTerminalShellPid(session: TerminalSession, pid: Int) {}

    override fun getTerminalCursorStyle(): Int? = null

    override fun logError(tag: String, message: String) = android.util.Log.e(tag, message)
    override fun logWarn(tag: String, message: String) = android.util.Log.w(tag, message)
    override fun logInfo(tag: String, message: String) = android.util.Log.i(tag, message)
    override fun logDebug(tag: String, message: String) = android.util.Log.d(tag, message)
    override fun logVerbose(tag: String, message: String) = android.util.Log.v(tag, message)
    override fun logStackTraceWithMessage(tag: String, message: String, e: Exception) = android.util.Log.e(tag, message, e)
    override fun logStackTrace(tag: String, e: Exception) = android.util.Log.e(tag, "stacktrace", e)

    override fun onScale(scale: Float): Float = scale.coerceIn(0.5f, 2.0f)
    override fun onSingleTapUp(e: MotionEvent) { terminalView.requestFocus() }
    override fun shouldBackButtonBeMappedToEscape(): Boolean = true
    override fun shouldEnforceCharBasedInput(): Boolean = false
    override fun shouldUseCtrlSpaceWorkaround(): Boolean = false
    override fun isTerminalViewSelected(): Boolean = true
    override fun copyModeChanged(copyMode: Boolean) {}
    override fun onKeyDown(keyCode: Int, e: KeyEvent, session: TerminalSession): Boolean = false
    override fun onKeyUp(keyCode: Int, e: KeyEvent): Boolean = false
    override fun onLongPress(event: MotionEvent): Boolean = false
    override fun readControlKey(): Boolean = false
    override fun readAltKey(): Boolean = false
    override fun readShiftKey(): Boolean = false
    override fun readFnKey(): Boolean = false
    override fun onCodePoint(codePoint: Int, ctrlDown: Boolean, session: TerminalSession): Boolean = false
    override fun onEmulatorSet() {}
}
