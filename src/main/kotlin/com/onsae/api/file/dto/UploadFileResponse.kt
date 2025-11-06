package com.onsae.api.file.dto

import com.onsae.api.file.entity.FileType
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

/**
 * 업로드 파일 정보 응답 DTO
 * 업로드된 파일의 상세 정보를 담는 응답 객체입니다.
 */
@Schema(description = "업로드 파일 정보")
data class UploadFileResponse(
    @Schema(description = "파일 ID")
    val id: Long,

    @Schema(description = "파일 타입", example = "IMAGE")
    val fileType: FileType,

    @Schema(description = "파일명", example = "health_check_2024.jpg")
    val fileName: String,

    @Schema(description = "원본 파일명", example = "건강검진결과지.jpg")
    val originalName: String?,

    @Schema(description = "파일 경로")
    val filePath: String,

    @Schema(description = "파일 크기 (bytes)", example = "1048576")
    val fileSize: Long?,

    @Schema(description = "MIME 타입", example = "image/jpeg")
    val mimeType: String?,

    @Schema(description = "재생 시간 (음성/비디오용, 초)", example = "120")
    val durationSeconds: Int?,

    @Schema(description = "이미지 너비 (이미지용, 픽셀)", example = "1920")
    val imageWidth: Int?,

    @Schema(description = "이미지 높이 (이미지용, 픽셀)", example = "1080")
    val imageHeight: Int?,

    @Schema(description = "썸네일 경로")
    val thumbnailPath: String?,

    @Schema(description = "업로드 순서", example = "1")
    val uploadOrder: Int,

    @Schema(description = "생성 일시")
    val createdAt: LocalDateTime
)

