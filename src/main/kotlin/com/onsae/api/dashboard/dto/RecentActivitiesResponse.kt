package com.onsae.api.dashboard.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "최근 활동 응답")
data class RecentActivitiesResponse(
    @Schema(description = "활동 목록")
    val activities: List<ActivityInfo>,

    @Schema(description = "페이지네이션")
    val pagination: PaginationInfo
)

@Schema(description = "활동 정보")
data class ActivityInfo(
    @Schema(description = "활동 ID")
    val id: String,

    @Schema(description = "활동 타입", example = "response")
    val type: String,

    @Schema(description = "사용자 정보")
    val user: UserBasicInfo,

    @Schema(description = "질문 정보 (응답 타입인 경우)")
    val question: QuestionBasicInfo?,

    @Schema(description = "업로드 정보 (업로드 타입인 경우)")
    val upload: UploadBasicInfo?,

    @Schema(description = "타임스탬프")
    val timestamp: LocalDateTime,

    @Schema(description = "상태", example = "completed")
    val status: String,

    @Schema(description = "우선순위", example = "normal")
    val priority: String
)

@Schema(description = "사용자 기본 정보")
data class UserBasicInfo(
    @Schema(description = "사용자 ID")
    val id: Long,

    @Schema(description = "이름")
    val name: String,

    @Schema(description = "사용자 코드")
    val code: String
)

@Schema(description = "질문 기본 정보")
data class QuestionBasicInfo(
    @Schema(description = "질문 ID")
    val id: Long,

    @Schema(description = "질문 제목")
    val title: String
)

@Schema(description = "업로드 기본 정보")
data class UploadBasicInfo(
    @Schema(description = "업로드 ID")
    val id: Long,

    @Schema(description = "파일 타입", example = "image")
    val type: String,

    @Schema(description = "파일명")
    val fileName: String
)

@Schema(description = "페이지네이션 정보")
data class PaginationInfo(
    @Schema(description = "총 개수")
    val total: Int,

    @Schema(description = "현재 페이지")
    val page: Int,

    @Schema(description = "페이지당 개수")
    val limit: Int
)
