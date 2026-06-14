package com.openflux.app.agent.tools

import com.openflux.app.agent.Tool
import com.openflux.app.model.ToolResult
import java.io.File

class FileTool : Tool {
    override val name: String = "write"
    override val description: String = "Write content to a file (creates new file or overwrites existing)"

    override suspend fun execute(args: Map<String, Any>): ToolResult {
        val filePath = args["filePath"] as? String ?: return ToolResult.error("Missing 'filePath' argument")
        val content = args["content"] as? String ?: return ToolResult.error("Missing 'content' argument")
        return try {
            val file = File(filePath)
            file.parentFile?.mkdirs()
            file.writeText(content)
            ToolResult.success("File written: $filePath (${content.length} bytes)")
        } catch (e: Exception) {
            ToolResult.error("Write failed: ${e.message}")
        }
    }

    override fun getSchema(): Map<String, Any> = mapOf(
        "name" to name,
        "description" to description,
        "parameters" to mapOf(
            "type" to "object",
            "properties" to mapOf(
                "filePath" to mapOf("type" to "string", "description" to "Absolute path to file"),
                "content" to mapOf("type" to "string", "description" to "File content to write")
            ),
            "required" to listOf("filePath", "content")
        )
    )
}
