package com.onsae.api.survey.dto

import java.time.LocalDateTime

data class QuestionAssignmentResponse(
    val id: Long,
    val questionId: Long,
    val questionTitle: String,
    val questionContent: String,
    val userId: Long?,
    val userName: String?,
    val groupId: Long?,
    val groupName: String?,
    val priority: Int,
    val assignedById: Long?,
    val assignedByName: String?,
    val assignedAt: LocalDateTime,
    val responseCount: Int
)