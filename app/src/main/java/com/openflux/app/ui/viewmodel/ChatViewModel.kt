package com.openflux.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openflux.app.agent.AgentEngine
import com.openflux.app.agent.AgentState
import com.openflux.app.model.Message
import com.openflux.app.model.Plan
import com.openflux.app.model.TokenUsage
import com.openflux.app.net.ApiClient
import com.openflux.app.net.SessionManager
import com.openflux.app.net.TokenTracker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    private val apiClient = ApiClient()
    private val sessionManager = SessionManager()
    private val tokenTracker = TokenTracker()
    private val agentEngine = AgentEngine(apiClient, sessionManager, tokenTracker)

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _agentState = MutableStateFlow(AgentState.IDLE)
    val agentState: StateFlow<AgentState> = _agentState.asStateFlow()

    private val _tokenUsage = MutableStateFlow(TokenUsage())
    val tokenUsage: StateFlow<TokenUsage> = _tokenUsage.asStateFlow()

    val plan: Plan get() = agentEngine.toolRegistry.plan

    init {
        viewModelScope.launch {
            agentEngine.state.collect { state ->
                _agentState.value = state
            }
        }
        viewModelScope.launch {
            tokenTracker.sessionUsage.collect { usage ->
                _tokenUsage.value = usage
            }
        }
        viewModelScope.launch {
            sessionManager.currentSession.collect { session ->
                _messages.value = session.messages.toList()
            }
        }
    }

    fun sendMessage() {
        val text = _inputText.value.trim()
        if (text.isEmpty()) return
        _inputText.value = ""
        viewModelScope.launch {
            agentEngine.processUserInput(text)
        }
    }

    fun updateInput(text: String) {
        _inputText.value = text
    }

    fun cancelResponse() {
        apiClient.cancelActive()
    }

    fun newSession() {
        agentEngine.reset()
    }

    fun getApiMessages() = sessionManager.getApiMessages()
}
