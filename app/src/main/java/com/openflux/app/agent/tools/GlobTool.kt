package com.openflux.app.agent.tools

import com.openflux.app.agent.Tool
import com.openflux.app.model.ToolResult
import java.io.File

class GlobTool : Tool {
    override val name: String = "glob"
    override val description: String = "Find files matching a glob pattern"

    override suspend fun execute(args: Map<String, Any>): ToolResult {
        val pattern = args["pattern"] as? String ?: return ToolResult.error("Missing 'pattern' argument")
        val path = args["path"] as? String ?: "."
        return try {
            val dir = File(path)
            if (!dir.exists()) return ToolResult.error("Directory not found: $path")

            // Convert glob to regex
            val regexStr = pattern.replace(".", "\\.")
                .replace("*", ".*")
                .replace("?", ".?")
            val regex = Regex(regexStr)

            val results = dir.walkTopDown()
                .filter { it.isFile && regex.matches(it.name) }
                .map { it.absolutePath }
                .toList()

            val output = results.joinToString("\n").let { it.ifEmpty { "No files matched pattern: $pattern" } }
            ToolResult.success(output)
        } catch (e: Exception) {
            ToolResult.error("Glob failed: ${e.message}")
        }
    }

    override fun getSchema(): Map<String, Any> = mapOf(
        "name" to name,
        "description" to description,
        "parameters" to mapOf(
            "type" to "object",
            "properties" to mapOf(
                "pattern" to mapOf("type" to "string", "description" to "Glob pattern (e.g. **/*.kt)"),
                "path" to mapOf("type" to "string", "description" to "Root directory for search")
            ),
            "required" to listOf("pattern")
        )
    )
}
