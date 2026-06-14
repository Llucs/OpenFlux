package com.openflux.app.agent

import com.openflux.app.model.Message
import com.openflux.app.model.ToolResult
import com.openflux.app.net.ApiClient
import com.openflux.app.net.SessionManager
import com.openflux.app.net.TokenTracker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

enum class AgentState {
    IDLE, THINKING, EXECUTING_TOOL, WAITING_INPUT, COMPACTING, ERROR
}

data class AgentAction(
    val type: String, // think, tool_call, tool_result, message, compact, error
    val content: String,
    val toolName: String? = null,
    val toolArgs: Map<String, Any>? = null,
    val toolResult: String? = null
)

class AgentEngine(
    private val apiClient: ApiClient,
    private val sessionManager: SessionManager,
    private val tokenTracker: TokenTracker,
    private val toolRegistry: ToolRegistry = ToolRegistry()
) {
    private val _state = MutableStateFlow(AgentState.IDLE)
    val state: StateFlow<AgentState> = _state.asStateFlow()

    private val _actions = MutableStateFlow<List<AgentAction>>(emptyList())
    val actions: StateFlow<List<AgentAction>> = _actions.asStateFlow()

    private var systemPrompt = buildDefaultSystemPrompt()
    private val pendingToolCalls = mutableMapOf<String, Map<String, Any>>()

    fun setSystemPrompt(prompt: String) {
        systemPrompt = prompt
    }

    suspend fun processUserInput(input: String): List<AgentAction> = withContext(Dispatchers.IO) {
        _state.value = AgentState.THINKING
        val actions = mutableListOf<AgentAction>()

        val userMsg = Message(role = "user", content = input)
        sessionManager.addMessage(userMsg)
        actions.add(AgentAction(type = "message", content = input))

        // Check compaction
        if (tokenTracker.needsCompaction()) {
            _state.value = AgentState.COMPACTING
            val summary = tokenTracker.getCompactSummary()
            sessionManager.compactSession(summary)
            actions.add(AgentAction(type = "compact", content = "Session compacted at ${tokenTracker.sessionUsage.value.totalTokens} tokens"))
            tokenTracker.resetSession()
        }

        try {
            val maxIterations = 10
            var shouldContinue = true
            var iteration = 0

            val initialMessages = buildMessages()

            // Get AI response
            _state.value = AgentState.THINKING
            val response = apiClient.complete(initialMessages)

            response.usage?.let { tokenTracker.track(it) }
            response.usage?.let { sessionManager.updateUsage(it) }

            val assistantMsg = Message(
                role = "assistant",
                content = response.content,
                reasoningContent = response.reasoningContent
            )
            sessionManager.addMessage(assistantMsg)
            actions.add(AgentAction(type = "think", content = response.content))

        } catch (e: Exception) {
            _state.value = AgentState.ERROR
            actions.add(AgentAction(type = "error", content = "Error: ${e.message ?: "Unknown error"}"))
        }

        _state.value = AgentState.IDLE
        _actions.value = actions
        actions
    }

    private fun buildMessages(): List<Message> {
        val msgs = mutableListOf<Message>()
        msgs.add(Message(role = "system", content = systemPrompt))
        msgs.addAll(sessionManager.currentSession.value.messages)
        return msgs
    }

    private fun buildDefaultSystemPrompt(): String = buildString {
        appendLine("You are OpenFlux, an advanced autonomous AI coding agent running on Android.")
        appendLine("You have access to a full Linux environment through Termux terminal integration.")
        appendLine()
        appendLine("## Available Tools")
        appendLine("- bash: Execute shell commands in the Termux environment")
        appendLine("- read: Read file contents")
        appendLine("- edit: Edit files with exact string replacement")
        appendLine("- write: Write new files")
        appendLine("- grep: Search file contents with regex")
        appendLine("- glob: Find files by glob pattern")
        appendLine("- webFetch: Fetch URL contents")
        appendLine("- webSearch: Search the web")
        appendLine("- task: Delegate subtasks to sub-agents")
        appendLine()
        appendLine("## Capabilities")
        appendLine("- Plan and execute complex multi-step tasks")
        appendLine("- Write, read, edit, and refactor code")
        appendLine("- Execute shell commands and scripts")
        appendLine("- Search codebases and files")
        appendLine("- Research using web search and fetch")
        appendLine("- Delegate work to sub-agents via the task tool")
        appendLine()
        appendLine("## Guidelines")
        appendLine("- First understand the full task before acting")
        appendLine("- Create a plan for complex tasks")
        appendLine("- Use tools strategically to accomplish goals")
        appendLine("- When you encounter errors, debug and fix them")
        appendLine("- Ask for clarification when needed")
        appendLine("- Think step by step for complex problems")
    }

    fun reset() {
        sessionManager.createNewSession()
        tokenTracker.resetSession()
        _state.value = AgentState.IDLE
        _actions.value = emptyList()
    }
}
