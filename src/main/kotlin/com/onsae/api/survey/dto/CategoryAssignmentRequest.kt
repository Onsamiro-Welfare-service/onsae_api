package com.onsae.api.survey.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class CategoryAssignmentRequest(
    @field:NotNull(message = "카테고리 ID는 필수입니다")
    val categoryId: Long,

    val userId: Long?,

    val groupId: Long?,

    @field:Min(value = 1, message = "우선순위는 1 이상이어야 합니다")
    val priority: Int = 5
)