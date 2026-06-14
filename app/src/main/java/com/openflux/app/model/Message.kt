package com.openflux.app.model

import java.util.UUID

data class Message(
    val id: String = UUID.randomUUID().toString(),
    val role: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val toolCalls: List<ToolCall>? = null,
    val toolCallId: String? = null,
    val reasoningContent: String? = null
)

data class ToolCall(
    val id: String = UUID.randomUUID().toString(),
    val type: String = "function",
    val function: FunctionCall
)

data class FunctionCall(
    val name: String,
    val arguments: String
)
