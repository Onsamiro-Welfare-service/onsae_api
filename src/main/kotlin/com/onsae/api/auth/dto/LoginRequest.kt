package com.onsae.api.auth.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

@Schema(description = "로그인 요청")
data class LoginRequest(
    @field:NotNull(message = "로그인 타입은 필수입니다")
    @Schema(description = "로그인 타입", example = "ADMIN")
    val loginType: LoginType,

    @field:Email(message = "올바른 이메일 형식이 아닙니다")
    @Schema(description = "이메일 (관리자 로그인용)", example = "admin@example.com")
    val email: String? = null,

    @Schema(description = "비밀번호 (관리자 로그인용)", example = "password123")
    val password: String? = null,

    @Schema(description = "로그인 코드 (사용자 로그인용)", example = "USER001")
    val loginCode: String? = null,

    @Schema(description = "기관 ID (Admin 로그인용)", example = "1")
    val institutionId: Long? = null
) {
    fun validate() {
        when (loginType) {
            LoginType.SYSTEM_ADMIN -> {
                require(!email.isNullOrBlank()) { "시스템 관리자 로그인에는 이메일이 필요합니다" }
                require(!password.isNullOrBlank()) { "시스템 관리자 로그인에는 비밀번호가 필요합니다" }
            }
            LoginType.ADMIN -> {
                require(!email.isNullOrBlank()) { "관리자 로그인에는 이메일이 필요합니다" }
                require(!password.isNullOrBlank()) { "관리자 로그인에는 비밀번호가 필요합니다" }
                require(institutionId != null) { "관리자 로그인에는 기관 ID가 필요합니다" }
            }
            LoginType.USER -> {
                require(!loginCode.isNullOrBlank()) { "사용자 로그인에는 로그인 코드가 필요합니다" }
            }
        }
    }
}