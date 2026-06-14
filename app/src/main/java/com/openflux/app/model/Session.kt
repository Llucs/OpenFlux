package com.openflux.app.model

import java.util.UUID

data class Session(
    val id: String = "ses_${UUID.randomUUID().toString().take(8)}",
    val messages: MutableList<Message> = mutableListOf(),
    var tokenUsage: TokenUsage = TokenUsage(),
    val createdAt: Long = System.currentTimeMillis(),
    var compacted: Boolean = false,
    var compactCount: Int = 0
) {
    val messageCount: Int get() = messages.size

    fun addMessage(message: Message) {
        messages.add(message)
    }

    fun getApiMessages(): List<Map<String, String?>> {
        return messages.map { msg ->
            mutableMapOf<String, String?>(
                "role" to msg.role,
                "content" to msg.content
            ).apply {
                if (msg.reasoningContent != null) {
                    put("reasoning_content", msg.reasoningContent)
                }
            }
        }
    }

    fun compact(summary: String) {
        val systemMsg = Message(role = "system", content = summary)
        messages.clear()
        messages.add(systemMsg)
        tokenUsage = TokenUsage()
        compacted = true
        compactCount++
    }
}
