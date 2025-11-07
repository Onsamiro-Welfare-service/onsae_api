package com.onsae.api.survey.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class QuestionAssignmentRequest(
    @field:NotNull(message = "질문 ID는 필수입니다")
    val questionId: Long,

    val userId: Long?,

    val groupId: Long?,

    @field:Min(value = 1, message = "우선순위는 1 이상이어야 합니다")
    val priority: Int = 5
)