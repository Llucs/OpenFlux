package com.openflux.app.agent

import com.openflux.app.model.ToolResult

interface Tool {
    val name: String
    val description: String
    suspend fun execute(args: Map<String, Any>): ToolResult
    fun getSchema(): Map<String, Any>
}
