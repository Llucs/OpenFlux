package com.openflux.app.agent

import com.openflux.app.agent.tools.BashTool
import com.openflux.app.agent.tools.EditTool
import com.openflux.app.agent.tools.FileTool
import com.openflux.app.agent.tools.GlobTool
import com.openflux.app.agent.tools.GrepTool
import com.openflux.app.agent.tools.PlanTool
import com.openflux.app.agent.tools.ReadTool
import com.openflux.app.agent.tools.TaskTool
import com.openflux.app.agent.tools.WebFetchTool
import com.openflux.app.agent.tools.WebSearchTool
import com.openflux.app.model.Plan

class ToolRegistry {
    private val tools = mutableMapOf<String, Tool>()
    val plan = Plan()

    init {
        register(BashTool())
        register(ReadTool())
        register(EditTool())
        register(FileTool())
        register(GrepTool())
        register(GlobTool())
        register(WebFetchTool())
        register(WebSearchTool())
        register(TaskTool())
        register(PlanTool(plan))
    }

    fun register(tool: Tool) {
        tools[tool.name] = tool
    }

    fun get(name: String): Tool? = tools[name]

    fun getAll(): List<Tool> = tools.values.toList()

    fun getToolSchemas(): List<Map<String, Any>> {
        return tools.values.map { tool ->
            mapOf(
                "type" to "function",
                "function" to tool.getSchema()
            )
        }
    }

    suspend fun execute(name: String, args: Map<String, Any>): ToolResult {
        val tool = get(name) ?: return ToolResult.error("Tool '$name' not found")
        return tool.execute(args)
    }
}
