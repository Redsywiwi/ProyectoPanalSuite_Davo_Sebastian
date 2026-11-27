package com.example.panalsuite.model
data class OperatorLoad(
    val operatorId: Long,
    val open: Int,
    val closed: Int
)

data class DashboardStats(
    val total: Int,
    val open: Int,
    val closed: Int,
    val assigned: Int,
    val openUnassigned: Int,
    val openCritical: Int,
    val byStatus: Map<TicketStatus, Int>,
    val byPriority: Map<Urgency, Int>,
    val createdLast7Days: Int,
    val oldestOpenAgeDays: Long?,
    val load: List<OperatorLoad>
)
