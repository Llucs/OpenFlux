package com.openflux.app.terminal

import android.util.Log
import com.termux.terminal.TerminalSession
import com.termux.view.TerminalView
import com.termux.view.TerminalViewClient

class TermuxTerminalManager(
    private val terminalView: TerminalView,
    shellPath: String = "/data/data/com.termux/files/usr/bin/bash",
    cwd: String = "~",
    var sessionCallback: SessionCallback? = null
) {
    private var terminalSession: TerminalSession? = null

    init {
        val env = arrayOf(
            "TERM=xterm-256color",
            "HOME=/data/data/com.termux/files/home",
            "PREFIX=/data/data/com.termux/files/usr",
            "PATH=/data/data/com.termux/files/usr/bin:/data/data/com.termux/files/usr/bin/applets:/system/bin:/system/xbin",
            "LD_LIBRARY_PATH=/data/data/com.termux/files/usr/lib",
            "LANG=en_US.UTF-8"
        )
        val args = arrayOf("-l")

        terminalSession = TerminalSession(
            shellPath,
            cwd,
            args,
            env,
            2000, // transcript rows
            null // TerminalSessionClient will be set after
        )

        terminalView.attachSession(terminalSession)

        terminalView.setTerminalViewClient(object : TerminalViewClient {
            override fun onScale(scale: Float): Float = scale
            override fun onSingleTapUp(e: android.view.MotionEvent?) {}
            override fun shouldBackButtonBeMappedToEscape(): Boolean = false
            override fun shouldEnforceCharBasedInput(): Boolean = true
            override fun shouldUseCtrlSpaceWorkaround(): Boolean = false
            override fun isTerminalViewSelected(): Boolean = true
            override fun copyModeChanged(copyMode: Boolean) {}
            override fun onKeyDown(keyCode: Int, e: android.view.KeyEvent?, session: TerminalSession?): Boolean = false
            override fun onKeyUp(keyCode: Int, e: android.view.KeyEvent?): Boolean = false
            override fun onLongPress(event: android.view.MotionEvent?): Boolean = false
            override fun readControlKey(): Boolean = false
            override fun readAltKey(): Boolean = false
            override fun readShiftKey(): Boolean = false
            override fun readFnKey(): Boolean = false
            override fun onCodePoint(codePoint: Int, ctrlDown: Boolean, session: TerminalSession?): Boolean = false
            override fun onEmulatorSet() {}
            override fun logError(tag: String?, message: String?) { Log.e(tag, message) }
            override fun logWarn(tag: String?, message: String?) { Log.w(tag, message) }
            override fun logInfo(tag: String?, message: String?) { Log.i(tag, message) }
            override fun logDebug(tag: String?, message: String?) { Log.d(tag, message) }
            override fun logVerbose(tag: String?, message: String?) { Log.v(tag, message) }
            override fun logStackTraceWithMessage(tag: String?, message: String?, e: java.lang.Exception?) { Log.e(tag, message, e) }
            override fun logStackTrace(tag: String?, e: java.lang.Exception?) { Log.e(tag, "stacktrace", e) }
        })
    }

    fun getSession(): TerminalSession? = terminalSession

    fun write(input: String) {
        terminalSession?.write(input, true)
    }

    fun finish() {
        terminalSession?.finishIfRunning()
    }

    interface SessionCallback {
        fun onSessionFinished()
        fun onTitleChanged(title: String?)
    }
}
