package com.onsae.api.survey.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CategoryRequest(
    @field:NotBlank(message = "카테고리 이름은 필수입니다")
    @field:Size(max = 100, message = "카테고리 이름은 100자 이하여야 합니다")
    val name: String,

    val description: String?,

    val imagePath: String?
)