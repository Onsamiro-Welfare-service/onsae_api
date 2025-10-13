package com.onsae.api.survey.dto

import com.onsae.api.survey.entity.QuestionType
import java.time.LocalDateTime

data class QuestionResponse(
    val id: Long,
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
    val isActive: Boolean,
    val institutionId: Long,
    val institutionName: String,
    val createdById: Long?,
    val createdByName: String?,
    val assignmentCount: Int,
    val responseCount: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)