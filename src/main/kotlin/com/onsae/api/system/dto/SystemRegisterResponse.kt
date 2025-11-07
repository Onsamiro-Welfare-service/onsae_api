package com.onsae.api.system.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "시스템 관리자 회원가입 응답")
data class SystemRegisterResponse(
    @Schema(description = "시스템 관리자 ID", example = "1")
    val id: Long,

    @Schema(description = "이름", example = "시스템관리자")
    val name: String,

    @Schema(description = "이메일", example = "admin@onsae.com")
    val email: String,

    @Schema(description = "활성화 상태", example = "true")
    val isActive: Boolean,

    @Schema(description = "생성일시")
    val createdAt: LocalDateTime
)