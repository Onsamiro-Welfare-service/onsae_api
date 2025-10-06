package com.onsae.api.admin.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

@Schema(description = "관리자 회원가입 요청")
data class AdminRegisterRequest(
    @field:NotBlank(message = "이름은 필수입니다")
    @field:Size(max = 50, message = "이름은 50자 이내여야 합니다")
    @Schema(description = "관리자 이름", example = "홍길동")
    val name: String,

    @field:NotBlank(message = "이메일은 필수입니다")
    @field:Email(message = "올바른 이메일 형식이 아닙니다")
    @field:Size(max = 100, message = "이메일은 100자 이내여야 합니다")
    @Schema(description = "이메일", example = "admin@example.com")
    val email: String,

    @field:NotBlank(message = "비밀번호는 필수입니다")
    @field:Size(min = 8, max = 100, message = "비밀번호는 8자 이상 100자 이내여야 합니다")
    @Schema(description = "비밀번호", example = "password123!")
    val password: String,

    @field:Size(max = 20, message = "전화번호는 20자 이내여야 합니다")
    @Schema(description = "전화번호", example = "010-1234-5678")
    val phone: String?,

    @field:NotNull(message = "역할은 필수입니다")
    @Schema(description = "관리자 역할")
    val role: AdminRole,

    @field:NotNull(message = "기관 ID는 필수입니다")
    @Schema(description = "소속 기관 ID", example = "1")
    val institutionId: Long
) {
    fun validate() {
        // 기본 유효성 검사는 어노테이션으로 처리
    }
}

@Schema(description = "관리자 역할")
enum class AdminRole {
    @Schema(description = "관리자")
    ADMIN,

    @Schema(description = "직원")
    STAFF
}

