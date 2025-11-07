package com.onsae.api.file.dto

import io.swagger.v3.oas.annotations.media.Schema

/**
 * 관리자 응답 요청 DTO
 * 관리자가 업로드된 콘텐츠에 대해 응답할 때 사용하는 요청 객체입니다.
 */
@Schema(description = "관리자 응답 요청")
data class AdminResponseRequest(
    @Schema(description = "관리자 응답 내용", example = "확인했습니다. 검토 후 연락드리겠습니다.")
    val response: String
)

