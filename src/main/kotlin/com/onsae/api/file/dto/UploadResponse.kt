package com.onsae.api.file.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

/**
 * 업로드 정보 응답 DTO
 * 업로드된 콘텐츠의 전체 정보를 담는 응답 객체입니다.
 */
@Schema(description = "업로드 정보")
data class UploadResponse(
    @Schema(description = "업로드 ID")
    val id: Long,

    @Schema(description = "업로드 제목")
    val title: String?,

    @Schema(description = "업로드 내용")
    val content: String?,

    @Schema(description = "업로드한 사용자 ID")
    val userId: Long?,

    @Schema(description = "업로드한 사용자 이름")
    val userName: String?,

    @Schema(description = "기관 ID")
    val institutionId: Long,

    @Schema(description = "기관명")
    val institutionName: String,

    @Schema(description = "관리자 확인 여부")
    val adminRead: Boolean,

    @Schema(description = "관리자 응답 내용")
    val adminResponse: String?,

    @Schema(description = "관리자 응답 일시")
    val adminResponseDate: LocalDateTime?,

    @Schema(description = "관리자 ID (응답한 관리자)")
    val adminId: Long?,

    @Schema(description = "관리자 이름 (응답한 관리자)")
    val adminName: String?,

    @Schema(description = "업로드된 파일 목록")
    val files: List<UploadFileResponse>,

    @Schema(description = "생성 일시")
    val createdAt: LocalDateTime,

    @Schema(description = "수정 일시")
    val updatedAt: LocalDateTime
)

