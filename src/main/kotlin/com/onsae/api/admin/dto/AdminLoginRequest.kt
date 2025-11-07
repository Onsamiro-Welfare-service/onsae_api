package com.onsae.api.admin.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

@Schema(description = "복지관 관리자 로그인 요청")
data class AdminLoginRequest(
    @field:NotBlank(message = "이메일은 필수입니다")
    @field:Email(message = "올바른 이메일 형식이 아닙니다")
    @Schema(description = "관리자 이메일", example = "admin@example.com")
    val email: String,

    @field:NotBlank(message = "비밀번호는 필수입니다")
    @Schema(description = "비밀번호", example = "password123")
    val password: String,

    @field:NotNull(message = "기관 ID는 필수입니다")
    @Schema(description = "소속 기관 ID", example = "1")
    val institutionId: Long
)