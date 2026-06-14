package com.openflux.app.model

data class ToolResult(
    val success: Boolean,
    val output: String,
    val error: String? = null,
    val exitCode: Int = 0
) {
    companion object {
        fun success(output: String, exitCode: Int = 0) = ToolResult(
            success = true, output = output, exitCode = exitCode
        )
        fun error(error: String, exitCode: Int = -1) = ToolResult(
            success = false, output = "", error = error, exitCode = exitCode
        )
    }
}
