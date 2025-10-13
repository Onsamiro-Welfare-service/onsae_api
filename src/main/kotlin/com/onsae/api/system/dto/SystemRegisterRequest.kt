package com.onsae.api.system.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

@Schema(description = "시스템 관리자 회원가입 요청")
data class SystemRegisterRequest(
    @field:NotBlank(message = "이름은 필수입니다")
    @field:Size(max = 10, message = "이름은 10자 이내여야 합니다")
    @Schema(description = "이름", example = "시스템관리자")
    val name: String,

    @field:NotBlank(message = "이메일은 필수입니다")
    @field:Email(message = "올바른 이메일 형식이 아닙니다")
    @field:Size(max = 100, message = "이메일은 100자 이내여야 합니다")
    @Schema(description = "이메일", example = "system@onsae.com")
    val email: String,


    @field:NotBlank(message = "비밀번호는 필수입니다")
    @field:Size(min = 8, max = 100, message = "비밀번호는 8자 이상 100자 이내여야 합니다")
    @Schema(description = "비밀번호", example = "admin123!")
    val password: String,

    @field:NotNull(message = "기관 ID는 필수입니다")
    @field:Positive(message = "기관 ID는 양수여야 합니다")
    @Schema(description = "기관 ID", example = "1")
    val institutionId: Long
)