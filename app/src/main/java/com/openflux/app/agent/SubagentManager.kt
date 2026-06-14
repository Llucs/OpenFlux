package com.openflux.app.agent

import com.openflux.app.model.Message
import com.openflux.app.model.ToolResult
import com.openflux.app.net.ApiClient
import com.openflux.app.net.TokenTracker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.UUID

data class SubagentTask(
    val id: String,
    val description: String,
    val prompt: String,
    val subagentType: String = "general",
    var result: ToolResult? = null,
    var status: SubagentStatus = SubagentStatus.PENDING
)

enum class SubagentStatus {
    PENDING, RUNNING, COMPLETED, FAILED
}

class SubagentManager(
    private val parentApiClient: ApiClient? = null
) {
    private val tasks = mutableMapOf<String, SubagentTask>()

    suspend fun delegateTask(
        description: String,
        prompt: String,
        subagentType: String = "general"
    ): SubagentTask = withContext(Dispatchers.IO) {
        val taskId = "task_${UUID.randomUUID().toString().take(12)}"
        val task = SubagentTask(
            id = taskId,
            description = description,
            prompt = prompt,
            subagentType = subagentType,
            status = SubagentStatus.PENDING
        )
        tasks[taskId] = task
        task
    }

    suspend fun executeTask(task: SubagentTask): ToolResult = withContext(Dispatchers.IO) {
        task.status = SubagentStatus.RUNNING
        try {
            val client = ApiClient(
                sessionId = task.id
            )

            val subagentPrompt = buildSubagentPrompt(task.subagentType)
            val messages = listOf(
                Message(role = "system", content = subagentPrompt),
                Message(role = "user", content = task.prompt)
            )

            val response = client.complete(messages)
            val result = ToolResult.success(
                "<task id=\"${task.id}\" state=\"completed\">\n" +
                "  <task_result>\n" +
                "    ${response.content.replace("\n", "\n    ")}\n" +
                "  </task_result>\n" +
                "</task>"
            )

            task.result = result
            task.status = SubagentStatus.COMPLETED
            result
        } catch (e: Exception) {
            val result = ToolResult.error(
                "<task id=\"${task.id}\" state=\"error\">\n" +
                "  <task_error>\n" +
                "    Subagent task failed: ${e.message}\n" +
                "  </task_error>\n" +
                "</task>"
            )
            task.result = result
            task.status = SubagentStatus.FAILED
            result
        }
    }

    suspend fun executeAll(tasks: List<SubagentTask>): List<ToolResult> = coroutineScope {
        tasks.map { task ->
            async {
                executeTask(task)
            }
        }.awaitAll()
    }

    private fun buildSubagentPrompt(type: String): String {
        return when (type) {
            "explore" -> "You are a fast agent specialized for exploring codebases. Search for files, read code, and answer questions concisely. Return only the information requested."
            else -> "You are a general-purpose sub-agent. Complete the assigned task thoroughly and return the result. Be concise but complete."
        }
    }

    fun getTask(id: String): SubagentTask? = tasks[id]

    fun getActiveTasks(): List<SubagentTask> = tasks.values.filter { it.status == SubagentStatus.RUNNING }

    fun clearCompleted() {
        tasks.values.removeAll { it.status == SubagentStatus.COMPLETED || it.status == SubagentStatus.FAILED }
    }
}
