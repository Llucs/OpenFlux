package com.openflux.app.agent.tools

import com.openflux.app.agent.SubagentManager
import com.openflux.app.agent.Tool
import com.openflux.app.model.ToolResult

class TaskTool(
    private val subagentManager: SubagentManager = SubagentManager()
) : Tool {
    override val name: String = "task"
    override val description: String = "Delegate a subtask to a sub-agent. Launches a new agent instance with its own context."

    override suspend fun execute(args: Map<String, Any>): ToolResult {
        val description = args["description"] as? String ?: return ToolResult.error("Missing 'description'")
        val prompt = args["prompt"] as? String ?: return ToolResult.error("Missing 'prompt'")
        val subagentType = args["subagent_type"] as? String ?: "general"

        return try {
            val task = subagentManager.delegateTask(description, prompt, subagentType)
            val result = subagentManager.executeTask(task)
            result
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
                "description" to mapOf("type" to "string", "description" to "A short (3-5 words) description of the task"),
                "prompt" to mapOf("type" to "string", "description" to "The task for the agent to perform"),
                "subagent_type" to mapOf(
                    "type" to "string",
                    "description" to "Type of subagent: general (multi-step), explore (fast code search)"
                )
            ),
            "required" to listOf("description", "prompt")
        )
    )
}
