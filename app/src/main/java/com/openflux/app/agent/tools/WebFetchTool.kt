package com.openflux.app.agent.tools

import com.openflux.app.agent.Tool
import com.openflux.app.model.ToolResult
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class WebFetchTool : Tool {
    override val name: String = "webFetch"
    override val description: String = "Fetch content from a URL"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    override suspend fun execute(args: Map<String, Any>): ToolResult {
        val url = args["url"] as? String ?: return ToolResult.error("Missing 'url' argument")
        return try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "OpenFlux/1.0.0")
                .build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            val preview = body.take(5000)
            ToolResult.success("Status: ${response.code}\n\n$preview")
        } catch (e: Exception) {
            ToolResult.error("Fetch failed: ${e.message}")
        }
    }

    override fun getSchema(): Map<String, Any> = mapOf(
        "name" to name,
        "description" to description,
        "parameters" to mapOf(
            "type" to "object",
            "properties" to mapOf(
                "url" to mapOf("type" to "string", "description" to "URL to fetch"),
                "format" to mapOf("type" to "string", "description" to "Response format (text, markdown, html)")
            ),
            "required" to listOf("url")
        )
    )
}
