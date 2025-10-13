package com.onsae.api.user.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "일반 사용자 로그인 요청")
data class UserLoginRequest(
    @field:NotBlank(message = "로그인 코드는 필수입니다")
    @Schema(description = "사용자 로그인 코드", example = "USER001")
    val loginCode: String
)