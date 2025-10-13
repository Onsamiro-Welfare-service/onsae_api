package com.onsae.api.survey.dto

import com.onsae.api.survey.entity.QuestionType
import java.time.LocalDateTime

data class UserQuestionResponse(
    val assignmentId: Long,
    val questionId: Long,
    val title: String,
    val content: String,
    val questionType: QuestionType,
    val categoryId: Long?,
    val categoryName: String?,
    val options: Map<String, Any>?,
    val allowOtherOption: Boolean,
    val otherOptionLabel: String,
    val otherOptionPlaceholder: String?,
    val isRequired: Boolean,
    val priority: Int,
    val assignmentSource: String, // "USER" or "GROUP"
    val sourceId: Long?, // userId or groupId
    val sourceName: String?, // userName or groupName
    val isCompleted: Boolean,
    val responseId: Long?,
    val responseAnswer: Map<String, Any>?,
    val responseSubmittedAt: LocalDateTime?,
    val assignedAt: LocalDateTime
)