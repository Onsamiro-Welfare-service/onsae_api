package com.onsae.api.auth.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "토큰 갱신 응답")
data class TokenResponse(
    @Schema(description = "새로운 액세스 토큰")
    val accessToken: String,

    @Schema(description = "토큰 타입", example = "Bearer")
    val tokenType: String = "Bearer",

    @Schema(description = "토큰 만료 시간")
    val expiresAt: LocalDateTime
)

@Schema(description = "토큰 갱신 요청")
data class RefreshTokenRequest(
    @Schema(description = "리프레시 토큰")
    val refreshToken: String
)