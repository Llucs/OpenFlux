package com.openflux.app.net

import com.openflux.app.model.TokenUsage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TokenTracker {
    private val _sessionUsage = MutableStateFlow(TokenUsage())
    val sessionUsage: StateFlow<TokenUsage> = _sessionUsage.asStateFlow()

    private val _totalUsage = MutableStateFlow(TokenUsage())
    val totalUsage: StateFlow<TokenUsage> = _totalUsage.asStateFlow()

    private val _sessionCount = MutableStateFlow(0)
    val sessionCount: StateFlow<Int> = _sessionCount.asStateFlow()

    fun track(usage: TokenUsage) {
        _sessionUsage.value = _sessionUsage.value + usage
        _totalUsage.value = _totalUsage.value + usage
        _sessionCount.value++
    }

    fun resetSession() {
        _sessionUsage.value = TokenUsage()
        _sessionCount.value = 0
    }

    fun resetAll() {
        _sessionUsage.value = TokenUsage()
        _totalUsage.value = TokenUsage()
        _sessionCount.value = 0
    }

    fun needsCompaction(): Boolean = _sessionUsage.value.needsCompaction

    fun getCompactSummary(): String {
        val usage = _sessionUsage.value
        return "Previous conversation context (${usage.totalTokens} tokens used across ${_sessionCount.value} messages). " +
               "Summary: The conversation was compacted to save context space. " +
               "Continue the task with full context preserved."
    }

    val remainingTokens: Int get() = _sessionUsage.value.remainingTokens
    val percentageUsed: Float get() = _sessionUsage.value.percentage
}
