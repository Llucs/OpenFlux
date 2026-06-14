package com.openflux.app.agent.tools

import com.openflux.app.agent.Tool
import com.openflux.app.model.ToolResult
import java.io.File

class GrepTool : Tool {
    override val name: String = "grep"
    override val description: String = "Search file contents using regex patterns"

    override suspend fun execute(args: Map<String, Any>): ToolResult {
        val pattern = args["pattern"] as? String ?: return ToolResult.error("Missing 'pattern' argument")
        val path = args["path"] as? String ?: "."
        val include = args["include"] as? String
        return try {
            val dir = File(path)
            if (!dir.exists()) return ToolResult.error("Directory not found: $path")

            val files = if (include != null) {
                dir.walkTopDown().filter { it.isFile && it.name.endsWith(include.removePrefix("*.")) }.toList()
            } else {
                dir.walkTopDown().filter { it.isFile }.toList()
            }

            val results = mutableListOf<String>()
            val regex = Regex(pattern)
            for (file in files.take(100)) {
                try {
                    file.useLines { lines ->
                        lines.forEachIndexed { index, line ->
                            if (regex.containsMatchIn(line)) {
                                results.add("${file.absolutePath}:${index + 1}: $line")
                            }
                        }
                    }
                } catch (_: Exception) {}
            }

            val output = results.joinToString("\n").let { it.ifEmpty { "No matches found" } }
            ToolResult.success(output)
        } catch (e: Exception) {
            ToolResult.error("Grep failed: ${e.message}")
        }
    }

    override fun getSchema(): Map<String, Any> = mapOf(
        "name" to name,
        "description" to description,
        "parameters" to mapOf(
            "type" to "object",
            "properties" to mapOf(
                "pattern" to mapOf("type" to "string", "description" to "Regex pattern to search"),
                "path" to mapOf("type" to "string", "description" to "Directory to search in"),
                "include" to mapOf("type" to "string", "description" to "File pattern filter (e.g. *.kt)")
            ),
            "required" to listOf("pattern")
        )
    )
}
