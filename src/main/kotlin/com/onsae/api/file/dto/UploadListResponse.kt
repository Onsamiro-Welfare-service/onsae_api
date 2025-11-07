package com.onsae.api.file.dto

import com.onsae.api.file.entity.FileType
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

/**
 * 업로드 목록 조회 응답 DTO
 * 여러 업로드 정보를 간단하게 보여주는 응답 객체입니다.
 */
@Schema(description = "업로드 목록 정보")
data class UploadListResponse(
    @Schema(description = "업로드 ID")
    val id: Long,

    @Schema(description = "업로드 제목")
    val title: String?,

    @Schema(description = "업로드 내용 (일부만 표시)")
    val contentPreview: String?,

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

    @Schema(description = "관리자 응답 일시")
    val adminResponseDate: LocalDateTime?,

    @Schema(description = "파일 개수")
    val fileCount: Int,

    @Schema(description = "최초 업로드된 파일 타입")
    val firstFileType: FileType?,

    @Schema(description = "생성 일시")
    val createdAt: LocalDateTime
)

