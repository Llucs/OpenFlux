package com.openflux.app.model

data class ApiResponse(
    val content: String,
    val model: String? = null,
    val usage: TokenUsage? = null,
    val reasoningContent: String? = null
)
