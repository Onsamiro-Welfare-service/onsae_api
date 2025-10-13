package com.onsae.api.survey.dto

import jakarta.validation.constraints.NotNull

data class QuestionResponseRequest(
    @field:NotNull(message = "할당 ID는 필수입니다")
    val assignmentId: Long,

    @field:NotNull(message = "응답 내용은 필수입니다")
    val answer: Map<String, Any>
)