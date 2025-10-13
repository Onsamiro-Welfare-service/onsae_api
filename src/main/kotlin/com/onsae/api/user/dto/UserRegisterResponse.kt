package com.onsae.api.user.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "사용자 등록 응답")
data class UserRegisterResponse(
    @Schema(description = "등록된 사용자 ID")
    val userId: Long,

    @Schema(description = "사용자 이름")
    val name: String,

    @Schema(description = "로그인 코드")
    val loginCode: String,

    @Schema(description = "기관 ID")
    val institutionId: Long,

    @Schema(description = "기관명")
    val institutionName: String,

    @Schema(description = "등록 일시")
    val createdAt: LocalDateTime,

    @Schema(description = "완료 메시지")
    val message: String = "사용자 등록이 완료되었습니다."
)