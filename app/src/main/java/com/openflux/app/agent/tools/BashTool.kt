package com.openflux.app.agent.tools

import com.openflux.app.agent.Tool
import com.openflux.app.model.ToolResult
import java.io.File

class BashTool : Tool {
    override val name: String = "bash"
    override val description: String = "Execute a shell command in the device environment"

    override suspend fun execute(args: Map<String, Any>): ToolResult {
        val command = args["command"] as? String ?: return ToolResult.error("Missing 'command' argument")
        return try {
            val process = ProcessBuilder()
                .command("sh", "-c", command)
                .redirectErrorStream(true)
                .start()
            val output = process.inputStream.bufferedReader().readText()
            val exitCode = process.waitFor()
            ToolResult.success(output.ifEmpty { "(no output)" }, exitCode)
        } catch (e: Exception) {
            ToolResult.error("Command failed: ${e.message}")
        }
    }

    override fun getSchema(): Map<String, Any> = mapOf(
        "name" to name,
        "description" to description,
        "parameters" to mapOf(
            "type" to "object",
            "properties" to mapOf(
                "command" to mapOf(
                    "type" to "string",
                    "description" to "Shell command to execute"
                )
            ),
            "required" to listOf("command")
        )
    )
}
