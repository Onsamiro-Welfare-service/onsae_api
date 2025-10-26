package com.onsae.api.survey.dto

import com.onsae.api.survey.entity.QuestionType
import java.time.LocalDateTime

data class QuestionResponseDetailDTO(
    val responseId: Long,
    val assignmentId: Long,
    val questionId: Long,
    val questionTitle: String,
    val questionContent: String,
    val questionType: QuestionType,
    val userId: Long,
    val userName: String,
    val responseData: Map<String, Any>,
    val responseText: String?,
    val otherResponse: String?,
    val responseTimeSeconds: Int?,
    val submittedAt: LocalDateTime,
    val ipAddress: String?,
    val userAgent: String?,
    val deviceInfo: Map<String, Any>?,
    val isModified: Boolean,  // 같은 날 중복 답변이 있으면 true
    val modificationCount: Int  // 같은 날 총 답변 횟수
)

data class UserResponseSummaryDTO(
    val userId: Long,
    val userName: String,
    val totalResponses: Int,
    val latestResponseAt: LocalDateTime?,
    val responses: List<QuestionResponseDetailDTO>
)

data class AssignmentResponseSummaryDTO(
    val assignmentId: Long,
    val questionId: Long,
    val questionTitle: String,
    val questionType: QuestionType,
    val totalResponses: Int,
    val responses: List<QuestionResponseDetailDTO>
)
