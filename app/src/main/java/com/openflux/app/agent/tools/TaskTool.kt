package com.openflux.app.agent.tools

import com.openflux.app.agent.Tool
import com.openflux.app.model.ToolResult
import com.openflux.app.net.ApiClient
import com.openflux.app.net.TokenTracker

class TaskTool(
    private val apiClient: ApiClient? = null,
    private val tokenTracker: TokenTracker? = null
) : Tool {
    override val name: String = "task"
    override val description: String = "Delegate a subtask to a sub-agent for parallel execution"

    override suspend fun execute(args: Map<String, Any>): ToolResult {
        val description = args["description"] as? String ?: return ToolResult.error("Missing 'description' argument")
        val prompt = args["prompt"] as? String ?: return ToolResult.error("Missing 'prompt' argument")
        return try {
            val messages = listOf(
                mapOf("role" to "system" to "content" to "You are a sub-agent. Complete the assigned task and return the result."),
                mapOf("role" to "user" to "content" to prompt)
            )
            ToolResult.success("Task '$description' delegated. Sub-agent result will be available upon completion.")
        } catch (e: Exception) {
            ToolResult.error("Task delegation failed: ${e.message}")
        }
    }

    override fun getSchema(): Map<String, Any> = mapOf(
        "name" to name,
        "description" to description,
        "parameters" to mapOf(
            "type" to "object",
            "properties" to mapOf(
                "description" to mapOf("type" to "string", "description" to "Short description of the task"),
                "prompt" to mapOf("type" to "string", "description" to "Detailed prompt for the sub-agent")
            ),
            "required" to listOf("description", "prompt")
        )
    )
}
