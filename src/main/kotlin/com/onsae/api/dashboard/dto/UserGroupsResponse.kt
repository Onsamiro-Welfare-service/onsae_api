package com.onsae.api.dashboard.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "사용자 그룹 현황 응답")
data class UserGroupsResponse(
    @Schema(description = "그룹 목록")
    val groups: List<GroupInfo>,

    @Schema(description = "총 멤버 수")
    val totalMembers: Long,

    @Schema(description = "그룹 미소속 멤버 수")
    val ungroupedMembers: Long
)

@Schema(description = "그룹 정보")
data class GroupInfo(
    @Schema(description = "그룹 ID")
    val groupId: Long,

    @Schema(description = "그룹명")
    val groupName: String,

    @Schema(description = "멤버 수")
    val memberCount: Int,

    @Schema(description = "활성 멤버 수")
    val activeMembers: Int,

    @Schema(description = "할당된 질문 수")
    val assignedQuestions: Int,

    @Schema(description = "완료된 응답 수")
    val completedResponses: Int,

    @Schema(description = "응답률(%)")
    val responseRate: Double,

    @Schema(description = "차트 색상", example = "#FF6B6B")
    val color: String
)
