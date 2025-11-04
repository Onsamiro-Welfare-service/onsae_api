package com.onsae.api.dashboard.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "사용자 그룹 현황 응답")
data class UserGroupsResponse(
    @Schema(description = "그룹 목록")
    val groups: List<GroupInfo>,

    @Schema(description = "총 멤버 수")
    val totalMembers: Long,

    @Schema(description = "사용자 분포 (원그래프용)")
    val userDistribution: UserDistribution
)

@Schema(description = "사용자 분포")
data class UserDistribution(
    @Schema(description = "그룹 조합별 사용자 분포")
    val categories: List<UserCategory>
)

@Schema(description = "사용자 카테고리 (그룹 조합)")
data class UserCategory(
    @Schema(description = "속한 그룹 ID 목록 (빈 배열 = 미소속)")
    val groupIds: List<Long>,

    @Schema(description = "속한 그룹 이름 목록")
    val groupNames: List<String>,

    @Schema(description = "이 조합에 속한 사용자 수")
    val userCount: Int,

    @Schema(description = "표시 라벨", example = "A그룹+B그룹")
    val label: String,

    @Schema(description = "차트 색상", example = "#FF6B6B")
    val color: String
)

@Schema(description = "그룹 정보")
data class GroupInfo(
    @Schema(description = "그룹 ID")
    val groupId: Long,

    @Schema(description = "그룹명")
    val groupName: String,

    @Schema(description = "멤버 수 (중복 포함)")
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
