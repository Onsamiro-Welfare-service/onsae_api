package com.onsae.api.system.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

@Schema(description = "시스템 관리자 로그인 요청")
data class SystemLoginRequest(
    @field:NotBlank(message = "이메일은 필수입니다")
    @field:Email(message = "올바른 이메일 형식이 아닙니다")
    @Schema(description = "시스템 관리자 이메일", example = "system@onsae.com")
    val email: String,

    @field:NotBlank(message = "비밀번호는 필수입니다")
    @Schema(description = "비밀번호", example = "admin123!")
    val password: String
)