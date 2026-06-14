package com.openflux.app.net

import com.openflux.app.BuildConfig
import com.openflux.app.model.ApiResponse
import com.openflux.app.model.Message
import com.openflux.app.model.TokenUsage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.UUID
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

class ApiClient(
    private val baseUrl: String = "https://opencode.ai/zen/v1/chat/completions",
    private val model: String = "deepseek-v4-flash-free",
    private val apiKey: String = "public",
    private val sessionId: String = "ses_${UUID.randomUUID().toString().take(8)}"
) {
    private val client = OkHttpClient.Builder()
        .callTimeout(300, TimeUnit.SECONDS)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(300, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val mediaType = "application/json".toMediaType()
    private val activeCall = AtomicReference<Call?>()

    fun cancelActive() {
        activeCall.getAndSet(null)?.cancel()
    }

    private fun buildRequestId(): String = "msg_${UUID.randomUUID().toString().take(12)}"

    private fun buildHeaders(): Map<String, String> = mapOf(
        "Content-Type" to "application/json",
        "Authorization" to "Bearer $apiKey",
        "User-Agent" to "OpenFlux/${BuildConfig.VERSION_NAME} ai-sdk/provider-utils/4.0.23",
        "x-opencode-client" to "android",
        "x-opencode-project" to "openflux",
        "x-opencode-session" to sessionId,
        "x-opencode-request" to buildRequestId()
    )

    private fun buildRequestBody(
        messages: List<Map<String, String?>>,
        stream: Boolean = false
    ): String {
        val body = JSONObject()
        body.put("model", model)
        body.put("messages", JSONArray().apply {
            messages.forEach { msg ->
                put(JSONObject().apply {
                    msg.forEach { (key, value) ->
                        if (value != null) put(key, value)
                    }
                })
            }
        })
        body.put("stream", stream)
        body.put("max_tokens", 4096)
        return body.toString()
    }

    suspend fun complete(messages: List<Message>): ApiResponse = withContext(Dispatchers.IO) {
        val apiMessages = messages.map { msg ->
            mutableMapOf<String, String?>(
                "role" to msg.role,
                "content" to msg.content
            ).apply {
                if (msg.reasoningContent != null) {
                    put("reasoning_content", msg.reasoningContent)
                }
            }
        }

        val bodyJson = buildRequestBody(apiMessages)
        val requestBuilder = Request.Builder().url(baseUrl).post(bodyJson.toRequestBody(mediaType))
        buildHeaders().forEach { (key, value) -> requestBuilder.header(key, value) }

        val respText = executeWithRetry(requestBuilder.build())
        val obj = JSONObject(respText)

        if (obj.has("error")) {
            throw IOException(obj.getJSONObject("error").optString("message", "Unknown API error"))
        }

        val usageJson = obj.optJSONObject("usage")
        val usage = usageJson?.let {
            TokenUsage(
                promptTokens = it.optInt("prompt_tokens", 0),
                completionTokens = it.optInt("completion_tokens", 0),
                totalTokens = it.optInt("total_tokens", 0)
            )
        }

        val modelName = obj.optString("model", null)?.takeIf { it.isNotBlank() }
        val choices = obj.optJSONArray("choices")

        if (choices != null && choices.length() > 0) {
            val choice = choices.getJSONObject(0)
            val msg = choice.optJSONObject("message")
            if (msg != null) {
                val content = msg.optString("content", "")
                val reasoningContent = msg.optString("reasoning_content", null)
                ApiResponse(content, modelName, usage, reasoningContent)
            } else {
                throw IOException("Invalid response: no message in choice")
            }
        } else {
            throw IOException("Invalid response: no choices")
        }
    }

    fun completeStream(messages: List<Message>): Flow<String> = callbackFlow {
        val apiMessages = messages.map { msg ->
            mutableMapOf<String, String?>(
                "role" to msg.role,
                "content" to msg.content
            ).apply {
                if (msg.reasoningContent != null) {
                    put("reasoning_content", msg.reasoningContent)
                }
            }
        }

        val bodyJson = buildRequestBody(apiMessages, stream = true)
        val requestBuilder = Request.Builder().url(baseUrl).post(bodyJson.toRequestBody(mediaType))
        buildHeaders().forEach { (key, value) -> requestBuilder.header(key, value) }

        val request = requestBuilder.build()
        val call = client.newCall(request)
        activeCall.set(call)

        val factory = EventSources.createFactory(client)
        val eventSource = factory.newEventSource(request, object : EventSourceListener() {
            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                if (data == "[DONE]") {
                    close()
                    return
                }
                try {
                    val obj = JSONObject(data)
                    val choices = obj.optJSONArray("choices")
                    if (choices != null && choices.length() > 0) {
                        val delta = choices.getJSONObject(0).optJSONObject("delta")
                        if (delta != null) {
                            val content = delta.optString("content", "")
                            if (content.isNotEmpty()) {
                                trySend(content)
                            }
                        }
                    }
                } catch (_: Exception) {}
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                if (t != null) close(t)
                else close()
            }

            override fun onClosed(eventSource: EventSource) {
                close()
            }
        })

        awaitClose {
            eventSource.cancel()
            activeCall.compareAndSet(call, null)
        }
    }

    private suspend fun executeWithRetry(request: Request): String = withContext(Dispatchers.IO) {
        var lastException: Exception? = null
        for (attempt in 1..3) {
            val call = client.newCall(request)
            activeCall.set(call)
            val job = Job()
            job.invokeOnCompletion { call.cancel() }
            try {
                return@withContext call.execute().use { resp ->
                    val txt = resp.body?.string().orEmpty()
                    if (!resp.isSuccessful) {
                        val errMsg = try {
                            val err = JSONObject(txt)
                            err.optJSONObject("error")?.optString("message", txt) ?: txt
                        } catch (_: Exception) { txt }
                        throw IOException("HTTP ${resp.code}: $errMsg")
                    }
                    if (txt.isBlank()) throw IOException("Empty response")
                    txt
                }
            } catch (e: Exception) {
                lastException = e
                if (attempt < 3) delay(attempt * 1000L)
            } finally {
                job.complete()
                activeCall.compareAndSet(call, null)
            }
        }
        throw lastException ?: IOException("Request failed after 3 retries")
    }
}
