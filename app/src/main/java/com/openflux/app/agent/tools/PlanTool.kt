package com.openflux.app.agent.tools

import com.openflux.app.agent.Tool
import com.openflux.app.model.Plan
import com.openflux.app.model.PlanItem
import com.openflux.app.model.PlanItemStatus
import com.openflux.app.model.ToolResult

class PlanTool(
    private val plan: Plan
) : Tool {
    override val name: String = "plan"
    override val description: String = "Create, update, and manage task plans with trackable items"

    override suspend fun execute(args: Map<String, Any>): ToolResult {
        return try {
            when (args["action"] as? String ?: "show") {
                "todowrite" -> handleTodowrite(args)
                "update" -> handleUpdate(args)
                "show" -> handleShow()
                else -> ToolResult.error("Unknown action: todowrite, update, show")
            }
        } catch (e: Exception) {
            ToolResult.error("Plan failed: ${e.message}")
        }
    }

    private fun handleTodowrite(args: Map<String, Any>): ToolResult {
        val itemsRaw = args["items"] as? List<*> ?: return ToolResult.error("Missing 'items' array")
        val newItems = mutableListOf<PlanItem>()

        for (raw in itemsRaw) {
            if (raw is Map<*, *>) {
                val content = raw["content"] as? String ?: continue
                val statusStr = (raw["status"] as? String)?.uppercase() ?: "PENDING"
                val priority = raw["priority"] as? String ?: "medium"
                val status = try {
                    PlanItemStatus.valueOf(statusStr)
                } catch (_: Exception) {
                    PlanItemStatus.PENDING
                }
                newItems.add(PlanItem(content = content, status = status, priority = priority))
            }
        }

        plan.items.clear()
        plan.items.addAll(newItems)

        return ToolResult.success(formatPlan())
    }

    private fun handleUpdate(args: Map<String, Any>): ToolResult {
        val index = (args["index"] as? Number)?.toInt() ?: return ToolResult.error("Missing 'index'")
        val statusStr = (args["status"] as? String)?.uppercase() ?: return ToolResult.error("Missing 'status'")

        if (index < 0 || index >= plan.items.size) {
            return ToolResult.error("Index $index out of range (0-${plan.items.size - 1})")
        }

        val status = try {
            PlanItemStatus.valueOf(statusStr)
        } catch (_: Exception) {
            return ToolResult.error("Invalid status: $statusStr. Use: PENDING, IN_PROGRESS, COMPLETED, CANCELLED")
        }

        val item = plan.items[index]
        if (args.containsKey("content")) {
            plan.items[index] = item.copy(content = args["content"] as String, status = status)
        } else {
            plan.items[index] = item.copy(status = status)
        }

        return ToolResult.success("Item $index updated to $statusStr\n\n${formatPlan()}")
    }

    private fun handleShow(): ToolResult {
        return if (plan.items.isEmpty()) {
            ToolResult.success("No plan set.")
        } else {
            ToolResult.success(formatPlan())
        }
    }

    private fun formatPlan(): String {
        val sb = StringBuilder("Plan:\n")
        for ((i, item) in plan.items.withIndex()) {
            val icon = when (item.status) {
                PlanItemStatus.PENDING -> "[ ]"
                PlanItemStatus.IN_PROGRESS -> "[~]"
                PlanItemStatus.COMPLETED -> "[x]"
                PlanItemStatus.CANCELLED -> "[-]"
            }
            sb.appendLine("  $i. $icon ${item.content} (${item.priority})")
        }
        return sb.toString()
    }

    override fun getSchema(): Map<String, Any> = mapOf(
        "name" to name,
        "description" to description,
        "parameters" to mapOf(
            "type" to "object",
            "properties" to mapOf(
                "action" to mapOf(
                    "type" to "string",
                    "description" to "Action: todowrite (set full plan), update (change one item), show (display)"
                ),
                "items" to mapOf(
                    "type" to "array",
                    "description" to "Array of plan items (for todowrite)",
                    "items" to mapOf(
                        "type" to "object",
                        "properties" to mapOf(
                            "content" to mapOf("type" to "string"),
                            "status" to mapOf("type" to "string", "enum" to listOf("pending", "in_progress", "completed", "cancelled")),
                            "priority" to mapOf("type" to "string", "enum" to listOf("high", "medium", "low"))
                        )
                    )
                ),
                "index" to mapOf("type" to "integer", "description" to "Item index to update (for update action)"),
                "status" to mapOf("type" to "string", "description" to "New status: pending, in_progress, completed, cancelled")
            ),
            "required" to listOf("action")
        )
    )
}
