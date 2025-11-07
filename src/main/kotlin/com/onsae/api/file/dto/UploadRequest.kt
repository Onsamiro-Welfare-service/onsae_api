package com.onsae.api.file.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Size

/**
 * 업로드 요청 DTO
 * 사용자가 파일을 업로드할 때 사용하는 요청 객체입니다.
 * 
 * @property title 업로드 제목 (선택사항)
 * @property content 업로드 내용 또는 설명 (선택사항)
 */
@Schema(description = "파일 업로드 요청")
data class UploadRequest(
    @field:Size(max = 200, message = "제목은 200자 이내여야 합니다")
    @Schema(description = "업로드 제목", example = "건강검진 결과지")
    val title: String? = null,

    @Schema(description = "업로드 내용 또는 설명", example = "2024년 건강검진 결과지를 업로드합니다.")
    val content: String? = null
)

