package com.onsae.api.auth.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

@Schema(description = "로그인 요청 기본 인터페이스")
sealed interface LoginRequestBase {
    val loginType: LoginType
}

@Schema(description = "시스템 관리자 로그인 요청")
data class SystemAdminLoginRequest(
    @field:NotBlank(message = "이메일은 필수입니다")
    @field:Email(message = "올바른 이메일 형식이 아닙니다")
    @Schema(description = "시스템 관리자 이메일", example = "system@onsae.com")
    val email: String,

    @field:NotBlank(message = "비밀번호는 필수입니다")
    @Schema(description = "비밀번호", example = "admin123!")
    val password: String,

    override val loginType: LoginType = LoginType.SYSTEM_ADMIN
) : LoginRequestBase

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
    val institutionId: Long,

    override val loginType: LoginType = LoginType.ADMIN
) : LoginRequestBase

@Schema(description = "일반 사용자 로그인 요청")
data class UserLoginRequest(
    @field:NotBlank(message = "로그인 코드는 필수입니다")
    @Schema(description = "사용자 로그인 코드", example = "USER001")
    val loginCode: String,

    override val loginType: LoginType = LoginType.USER
) : LoginRequestBase

@Schema(description = "로그인 요청 래퍼")
data class LoginRequestWrapper(
    @Schema(description = "로그인 타입", example = "ADMIN")
    val loginType: LoginType,

    @Schema(description = "시스템 관리자 로그인 요청")
    val systemAdminLogin: SystemAdminLoginRequest? = null,

    @Schema(description = "복지관 관리자 로그인 요청")
    val adminLogin: AdminLoginRequest? = null,

    @Schema(description = "일반 사용자 로그인 요청")
    val userLogin: UserLoginRequest? = null
) {
    fun getLoginRequest(): LoginRequestBase {
        return when (loginType) {
            LoginType.SYSTEM_ADMIN -> systemAdminLogin ?: throw IllegalArgumentException("시스템 관리자 로그인 정보가 필요합니다")
            LoginType.ADMIN -> adminLogin ?: throw IllegalArgumentException("관리자 로그인 정보가 필요합니다")
            LoginType.USER -> userLogin ?: throw IllegalArgumentException("사용자 로그인 정보가 필요합니다")
        }
    }
}