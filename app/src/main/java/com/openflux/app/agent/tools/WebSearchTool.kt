package com.openflux.app.agent.tools

import com.openflux.app.agent.Tool
import com.openflux.app.model.ToolResult

class WebSearchTool : Tool {
    override val name: String = "webSearch"
    override val description: String = "Search the web for information"

    override suspend fun execute(args: Map<String, Any>): ToolResult {
        val query = args["query"] as? String ?: return ToolResult.error("Missing 'query' argument")
        return try {
            val url = java.net.URLEncoder.encode(query, "UTF-8")
            val searchUrl = "https://html.duckduckgo.com/html/?q=$url"
            val fetchTool = WebFetchTool()
            fetchTool.execute(mapOf("url" to searchUrl))
        } catch (e: Exception) {
            ToolResult.error("Web search failed: ${e.message}")
        }
    }

    override fun getSchema(): Map<String, Any> = mapOf(
        "name" to name,
        "description" to description,
        "parameters" to mapOf(
            "type" to "object",
            "properties" to mapOf(
                "query" to mapOf("type" to "string", "description" to "Search query"),
                "numResults" to mapOf("type" to "integer", "description" to "Number of results")
            ),
            "required" to listOf("query")
        )
    )
}
