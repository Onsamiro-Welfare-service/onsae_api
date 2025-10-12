package com.onsae.api.dashboard.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "대시보드 핵심 통계 응답")
data class DashboardStatsResponse(
    @Schema(description = "총 사용자 수")
    val totalUsers: Int,

    @Schema(description = "활성 사용자 수")
    val activeUsers: Int,

    @Schema(description = "총 사용자 변화량")
    val totalUsersChange: ChangeInfo,

    @Schema(description = "활성 사용자 변화량")
    val activeUsersChange: ChangeInfo,

    @Schema(description = "오늘 응답 현황")
    val todayResponses: TodayResponseInfo,

    @Schema(description = "미처리 업로드")
    val pendingUploads: PendingInfo,

    @Schema(description = "승인 대기")
    val pendingApprovals: ApprovalInfo
)

@Schema(description = "변화 정보")
data class ChangeInfo(
    @Schema(description = "변화량", example = "12")
    val value: Int,

    @Schema(description = "기간", example = "이번 주")
    val period: String
)

@Schema(description = "오늘 응답 정보")
data class TodayResponseInfo(
    @Schema(description = "총 응답 수")
    val total: Int,

    @Schema(description = "할당된 질문 수")
    val assigned: Int,

    @Schema(description = "응답률(%)")
    val rate: Double,

    @Schema(description = "변화량")
    val change: Int
)

@Schema(description = "미처리 정보")
data class PendingInfo(
    @Schema(description = "개수")
    val count: Int,

    @Schema(description = "변화량")
    val change: Int
)

@Schema(description = "승인 대기 정보")
data class ApprovalInfo(
    @Schema(description = "관리자 승인 대기")
    val admins: Int,

    @Schema(description = "복지관 승인 대기")
    val welfareCenters: Int
)
