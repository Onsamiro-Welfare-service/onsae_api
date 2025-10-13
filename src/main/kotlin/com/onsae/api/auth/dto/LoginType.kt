package com.onsae.api.auth.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "로그인 타입")
enum class LoginType {
    @Schema(description = "시스템 관리자")
    SYSTEM_ADMIN,

    @Schema(description = "복지관 관리자")
    ADMIN,

    @Schema(description = "일반 사용자")
    USER
}