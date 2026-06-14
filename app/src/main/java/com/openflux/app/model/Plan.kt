package com.openflux.app.model

data class Plan(
    val items: MutableList<PlanItem> = mutableListOf()
)

data class PlanItem(
    val content: String,
    var status: PlanItemStatus = PlanItemStatus.PENDING,
    val priority: String = "medium"
)

enum class PlanItemStatus {
    PENDING, IN_PROGRESS, COMPLETED, CANCELLED
}
