package com.openflux.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.openflux.app.terminal.TerminalPresenter

class TerminalViewModel(application: Application) : AndroidViewModel(application) {
    val presenter = TerminalPresenter(application)

    fun initializeTerminal() {
        presenter.initializeSession()
    }

    override fun onCleared() {
        super.onCleared()
        presenter.destroy()
    }
}
