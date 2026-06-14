package com.openflux.app.agent

import com.openflux.app.model.Message
import com.openflux.app.net.ApiClient
import com.openflux.app.net.SessionManager
import com.openflux.app.net.TokenTracker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

enum class AgentState {
    IDLE, THINKING, EXECUTING_TOOL, COMPACTING, ERROR
}

data class AgentAction(
    val type: String,
    val content: String,
    val toolName: String? = null,
    val toolArgs: Map<String, Any>? = null,
    val toolResult: String? = null
)

class AgentEngine(
    private val apiClient: ApiClient,
    private val sessionManager: SessionManager,
    private val tokenTracker: TokenTracker,
    val toolRegistry: ToolRegistry = ToolRegistry()
) {
    private val _state = MutableStateFlow(AgentState.IDLE)
    val state: StateFlow<AgentState> = _state.asStateFlow()

    private val _actions = MutableStateFlow<List<AgentAction>>(emptyList())
    val actions: StateFlow<List<AgentAction>> = _actions.asStateFlow()

    private var systemPrompt = buildDefaultSystemPrompt()

    fun setSystemPrompt(prompt: String) {
        systemPrompt = prompt
    }

    suspend fun processUserInput(input: String) = withContext(Dispatchers.IO) {
        _state.value = AgentState.THINKING
        val actions = mutableListOf<AgentAction>()

        val userMsg = Message(role = "user", content = input)
        sessionManager.addMessage(userMsg)
        actions.add(AgentAction(type = "message", content = input))

        if (tokenTracker.needsCompaction()) {
            _state.value = AgentState.COMPACTING
            val summary = tokenTracker.getCompactSummary()
            sessionManager.compactSession(summary)
            actions.add(AgentAction(type = "compact", content = "Session compacted at ${tokenTracker.sessionUsage.value.totalTokens} tokens"))
            tokenTracker.resetSession()
        }

        try {
            for (iteration in 1..25) {
                _state.value = AgentState.THINKING
                val messages = buildMessages()
                val response = apiClient.complete(messages)

                response.usage?.let { tokenTracker.track(it) }
                response.usage?.let { sessionManager.updateUsage(it) }

                val toolCalls = parseToolCalls(response.content)
                if (toolCalls.isEmpty()) {
                    val assistantMsg = Message(
                        role = "assistant",
                        content = response.content,
                        reasoningContent = response.reasoningContent
                    )
                    sessionManager.addMessage(assistantMsg)
                    actions.add(AgentAction(type = "think", content = response.content))
                    break
                }

                val thinkContent = response.content.substringBefore("<tool_call>").trim()
                if (thinkContent.isNotEmpty()) {
                    val thinkingMsg = Message(role = "assistant", content = thinkContent)
                    sessionManager.addMessage(thinkingMsg)
                    actions.add(AgentAction(type = "think", content = thinkContent))
                }

                for ((toolName, toolArgs) in toolCalls) {
                    _state.value = AgentState.EXECUTING_TOOL
                    val result = toolRegistry.execute(toolName, toolArgs)
                    val resultMsg = Message(
                        role = "tool",
                        content = result.output,
                        toolCallId = toolName
                    )
                    sessionManager.addMessage(resultMsg)
                    actions.add(AgentAction(
                        type = "tool_result",
                        content = "$toolName executed",
                        toolName = toolName,
                        toolArgs = toolArgs,
                        toolResult = result.output
                    ))
                }
            }
        } catch (e: Exception) {
            _state.value = AgentState.ERROR
            actions.add(AgentAction(type = "error", content = "Error: ${e.message ?: "Unknown error"}"))
        }

        _state.value = AgentState.IDLE
        _actions.value = actions
    }

    private fun parseToolCalls(content: String): List<Pair<String, Map<String, Any>>> {
        val calls = mutableListOf<Pair<String, Map<String, Any>>>()
        val pattern = Regex("<tool_call>\\s*\\{[^}]+\\}", RegexOption.DOT_MATCHES_ALL)
        val matches = pattern.findAll(content)
        for (match in matches) {
            try {
                val json = match.value.removePrefix("<tool_call>").trim()
                val toolName = Regex("\"tool\"\\s*:\\s*\"([^\"]+)\"").find(json)?.groupValues?.get(1)
                val argsJson = Regex("\"args\"\\s*:\\s*\\{([^}]+)\\}").find(json)
                if (toolName != null && argsJson != null) {
                    val args = mutableMapOf<String, Any>()
                    val argPairs = Regex("\"([^\"]+)\"\\s*:\\s*\"([^\"]*)\"").findAll(argsJson.value)
                    for (arg in argPairs) {
                        args[arg.groupValues[1]] = arg.groupValues[2]
                    }
                    calls.add(toolName to args)
                }
            } catch (_: Exception) {}
        }
        return calls
    }

    private fun buildMessages(): List<Message> {
        val msgs = mutableListOf<Message>()
        msgs.add(Message(role = "system", content = systemPrompt))
        msgs.addAll(sessionManager.currentSession.value.messages)
        return msgs
    }

    private fun buildDefaultSystemPrompt(): String = buildString {
        appendLine("You are OpenFlux, an autonomous AI coding agent running on Android with Termux terminal integration.")
        appendLine()
        appendLine("## Available Tools")
        for (tool in toolRegistry.getAll()) {
            appendLine("- ${tool.name}: ${tool.description}")
        }
        appendLine()
        appendLine("## How to Use Tools")
        appendLine("To use a tool, output:")
        appendLine("<tool_call>")
        appendLine("{\"tool\": \"toolName\", \"args\": {\"key\": \"value\"}}")
        appendLine("</tool_call>")
        appendLine()
        appendLine("## Planning")
        appendLine("For complex tasks, create a plan first using the plan tool with action=todowrite and items array.")
        appendLine("Update plan item status as you complete them.")
        appendLine()
        appendLine("## Guidelines")
        appendLine("- Understand the full task before acting")
        appendLine("- Create plans for multi-step tasks")
        appendLine("- Use tools strategically")
        appendLine("- Debug errors and retry")
        appendLine("- Think step by step")
    }

    fun reset() {
        sessionManager.createNewSession()
        tokenTracker.resetSession()
        toolRegistry.plan.items.clear()
        _state.value = AgentState.IDLE
        _actions.value = emptyList()
    }
}
