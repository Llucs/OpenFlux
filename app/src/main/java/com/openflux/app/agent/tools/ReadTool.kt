package com.openflux.app.agent.tools

import com.openflux.app.agent.Tool
import com.openflux.app.model.ToolResult
import java.io.File

class ReadTool : Tool {
    override val name: String = "read"
    override val description: String = "Read file contents from the filesystem"

    override suspend fun execute(args: Map<String, Any>): ToolResult {
        val filePath = args["filePath"] as? String ?: return ToolResult.error("Missing 'filePath' argument")
        val offset = (args["offset"] as? Number)?.toInt() ?: 0
        val limit = (args["limit"] as? Number)?.toInt() ?: 2000
        return try {
            val file = File(filePath)
            if (!file.exists()) return ToolResult.error("File not found: $filePath")
            if (!file.isFile()) return ToolResult.error("Not a file: $filePath")
            val lines = file.readLines()
            val start = offset.coerceIn(0, lines.size)
            val end = (start + limit).coerceIn(0, lines.size)
            val content = lines.subList(start, end).mapIndexed { i, line -> "${start + i + 1}: $line" }.joinToString("\n")
            ToolResult.success(content.ifEmpty { "(empty file)" })
        } catch (e: Exception) {
            ToolResult.error("Read failed: ${e.message}")
        }
    }

    override fun getSchema(): Map<String, Any> = mapOf(
        "name" to name,
        "description" to description,
        "parameters" to mapOf(
            "type" to "object",
            "properties" to mapOf(
                "filePath" to mapOf("type" to "string", "description" to "Absolute path to file"),
                "offset" to mapOf("type" to "integer", "description" to "Starting line number (1-indexed)"),
                "limit" to mapOf("type" to "integer", "description" to "Max lines to read")
            ),
            "required" to listOf("filePath")
        )
    )
}
