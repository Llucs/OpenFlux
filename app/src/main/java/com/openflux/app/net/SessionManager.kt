package com.openflux.app.net

import com.openflux.app.model.Message
import com.openflux.app.model.Session
import com.openflux.app.model.TokenUsage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionManager {
    private val _currentSession = MutableStateFlow(Session())
    val currentSession: StateFlow<Session> = _currentSession.asStateFlow()

    private val _sessions = mutableListOf<Session>()
    val sessions: List<Session> get() = _sessions.toList()

    fun createNewSession() {
        val oldSession = _currentSession.value
        if (oldSession.messages.isNotEmpty()) {
            _sessions.add(oldSession.copy(messages = ArrayList(oldSession.messages)))
        }
        _currentSession.value = Session()
    }

    fun addMessage(message: Message) {
        val session = _currentSession.value
        session.addMessage(message)
        _currentSession.value = session.copy(messages = ArrayList(session.messages))
    }

    fun updateUsage(usage: TokenUsage) {
        val session = _currentSession.value
        session.tokenUsage = session.tokenUsage + usage
        _currentSession.value = session.copy(tokenUsage = session.tokenUsage)
    }

    fun compactSession(summary: String) {
        val session = _currentSession.value
        session.compact(summary)
        _currentSession.value = session.copy(
            messages = ArrayList(session.messages),
            tokenUsage = session.tokenUsage,
            compacted = session.compacted,
            compactCount = session.compactCount
        )
    }

    fun switchToSession(index: Int) {
        if (index in _sessions.indices) {
            val oldSession = _currentSession.value
            if (oldSession.messages.isNotEmpty()) {
                _sessions[if (_sessions.indexOf(oldSession) >= 0) _sessions.indexOf(oldSession) else -1]?.let {
                    _sessions[_sessions.indexOf(oldSession)] = oldSession.copy(messages = ArrayList(oldSession.messages))
                }
            }
            _currentSession.value = _sessions[index].copy(messages = ArrayList(_sessions[index].messages))
        }
    }

    fun deleteSession(index: Int) {
        if (index in _sessions.indices) {
            _sessions.removeAt(index)
        }
    }

    fun getApiMessages(): List<Map<String, String?>> {
        return _currentSession.value.getApiMessages()
    }
}
