package com.onsae.api.auth.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "로그인 응답")
data class LoginResponse(
    @Schema(description = "액세스 토큰")
    val accessToken: String,

    @Schema(description = "리프레시 토큰")
    val refreshToken: String,

    @Schema(description = "토큰 타입", example = "Bearer")
    val tokenType: String = "Bearer",

    @Schema(description = "액세스 토큰 만료 시간")
    val expiresAt: LocalDateTime,

    @Schema(description = "사용자 정보")
    val user: UserInfo
)

@Schema(description = "사용자 정보")
data class UserInfo(
    @Schema(description = "사용자 ID")
    val id: Long,

    @Schema(description = "사용자 타입")
    val userType: String,

    @Schema(description = "이름")
    val name: String,

    @Schema(description = "이메일")
    val email: String?,

    @Schema(description = "기관 ID")
    val institutionId: Long?,

    @Schema(description = "기관명")
    val institutionName: String?,

    @Schema(description = "권한 목록")
    val authorities: List<String>
)