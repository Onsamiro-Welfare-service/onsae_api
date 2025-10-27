package com.onsae.api.user.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "사용자 회원가입 응답")
data class UserSignupResponse(
    @Schema(description = "등록된 사용자 ID")
    val userId: Long,

    @Schema(description = "사용자명")
    val username: String,

    @Schema(description = "사용자 이름")
    val name: String,

    @Schema(description = "기관 ID")
    val institutionId: Long,

    @Schema(description = "기관명")
    val institutionName: String,

    @Schema(description = "가입 일시")
    val createdAt: LocalDateTime,

    @Schema(description = "완료 메시지")
    val message: String = "회원가입이 완료되었습니다. 로그인해주세요."
)
