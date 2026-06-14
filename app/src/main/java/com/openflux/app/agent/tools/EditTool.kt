package com.openflux.app.agent.tools

import com.openflux.app.agent.Tool
import com.openflux.app.model.ToolResult
import java.io.File

class EditTool : Tool {
    override val name: String = "edit"
    override val description: String = "Edit a file using exact string replacement"

    override suspend fun execute(args: Map<String, Any>): ToolResult {
        val filePath = args["filePath"] as? String ?: return ToolResult.error("Missing 'filePath' argument")
        val oldString = args["oldString"] as? String ?: return ToolResult.error("Missing 'oldString' argument")
        val newString = args["newString"] as? String ?: return ToolResult.error("Missing 'newString' argument")
        return try {
            val file = File(filePath)
            if (!file.exists()) return ToolResult.error("File not found: $filePath")
            var content = file.readText()
            if (!content.contains(oldString)) {
                return ToolResult.error("oldString not found in file content")
            }
            content = content.replaceFirst(oldString, newString)
            file.writeText(content)
            ToolResult.success("File edited successfully at $filePath")
        } catch (e: Exception) {
            ToolResult.error("Edit failed: ${e.message}")
        }
    }

    override fun getSchema(): Map<String, Any> = mapOf(
        "name" to name,
        "description" to description,
        "parameters" to mapOf(
            "type" to "object",
            "properties" to mapOf(
                "filePath" to mapOf("type" to "string", "description" to "Absolute path to file"),
                "oldString" to mapOf("type" to "string", "description" to "Text to replace"),
                "newString" to mapOf("type" to "string", "description" to "Replacement text")
            ),
            "required" to listOf("filePath", "oldString", "newString")
        )
    )
}
