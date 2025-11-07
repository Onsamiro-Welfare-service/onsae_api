package com.onsae.api.user.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "일반 사용자 로그인 요청")
data class UserLoginRequest(
    @field:NotBlank(message = "사용자명은 필수입니다")
    @Schema(description = "사용자명", example = "user001")
    val username: String,

    @field:NotBlank(message = "비밀번호는 필수입니다")
    @Schema(description = "비밀번호", example = "password123!")
    val password: String
)