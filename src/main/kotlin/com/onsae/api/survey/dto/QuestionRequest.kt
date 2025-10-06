package com.onsae.api.survey.dto

import com.onsae.api.survey.entity.QuestionType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class QuestionRequest(
    @field:NotBlank(message = "질문 제목은 필수입니다")
    @field:Size(max = 200, message = "질문 제목은 200자 이하여야 합니다")
    val title: String,

    @field:NotBlank(message = "질문 내용은 필수입니다")
    val content: String,

    val questionType: QuestionType,

    val categoryId: Long?,

    val options: Map<String, Any>?,

    val allowOtherOption: Boolean = false,

    @field:Size(max = 50, message = "기타 옵션 라벨은 50자 이하여야 합니다")
    val otherOptionLabel: String = "기타",

    @field:Size(max = 100, message = "기타 옵션 플레이스홀더는 100자 이하여야 합니다")
    val otherOptionPlaceholder: String?,

    val isRequired: Boolean = false
)