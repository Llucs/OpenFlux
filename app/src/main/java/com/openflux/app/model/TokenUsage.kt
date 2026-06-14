package com.openflux.app.model

data class TokenUsage(
    val promptTokens: Int = 0,
    val completionTokens: Int = 0,
    val totalTokens: Int = 0
) {
    companion object {
        val MAX_CONTEXT_TOKENS = 200_000
        val COMPACTION_THRESHOLD = 180_000
        val RESERVED_OUTPUT_TOKENS = 16_000
    }

    val remainingTokens: Int
        get() = MAX_CONTEXT_TOKENS - totalTokens - RESERVED_OUTPUT_TOKENS

    val needsCompaction: Boolean
        get() = totalTokens >= COMPACTION_THRESHOLD

    val percentage: Float
        get() = totalTokens.toFloat() / MAX_CONTEXT_TOKENS.toFloat()

    operator fun plus(other: TokenUsage): TokenUsage = TokenUsage(
        promptTokens = promptTokens + other.promptTokens,
        completionTokens = completionTokens + other.completionTokens,
        totalTokens = totalTokens + other.totalTokens
    )
}
