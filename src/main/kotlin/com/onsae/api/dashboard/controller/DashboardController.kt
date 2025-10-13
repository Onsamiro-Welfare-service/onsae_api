package com.onsae.api.dashboard.controller

import com.onsae.api.auth.security.CustomUserPrincipal
import com.onsae.api.dashboard.dto.DashboardStatsResponse
import com.onsae.api.dashboard.dto.RecentActivitiesResponse
import com.onsae.api.dashboard.dto.ResponseTrendsResponse
import com.onsae.api.dashboard.dto.UserGroupsResponse
import com.onsae.api.dashboard.service.DashboardService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "대시보드", description = "대시보드 통계 및 요약 정보 API")
class DashboardController(
    private val dashboardService: DashboardService
) {

    @GetMapping("/stats")
    @Operation(
        summary = "대시보드 핵심 통계 조회",
        description = "대시보드에 표시할 핵심 통계 지표를 조회합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "통계 조회 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "권한 부족")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    fun getDashboardStats(authentication: Authentication): ResponseEntity<DashboardStatsResponse> {
        val principal = authentication.principal as CustomUserPrincipal

        logger.info("Dashboard stats request by user: ${principal.userId}")

        val stats = dashboardService.getDashboardStats(principal.institutionId!!)

        return ResponseEntity.ok(stats)
    }

    @GetMapping("/response-trends")
    @Operation(
        summary = "응답 추이 조회",
        description = "일별 응답 현황 추이를 조회합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "응답 추이 조회 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "권한 부족")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    fun getResponseTrends(
        @RequestParam(defaultValue = "7d") period: String,
        authentication: Authentication
    ): ResponseEntity<ResponseTrendsResponse> {
        val principal = authentication.principal as CustomUserPrincipal

        logger.info("Response trends request by user: ${principal.userId}, period: $period")

        val trends = dashboardService.getResponseTrends(principal.institutionId!!, period)

        return ResponseEntity.ok(trends)
    }

    @GetMapping("/user-groups")
    @Operation(
        summary = "사용자 그룹 현황 조회",
        description = "그룹별 사용자 현황 및 응답률을 조회합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "그룹 현황 조회 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "권한 부족")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    fun getUserGroups(authentication: Authentication): ResponseEntity<UserGroupsResponse> {
        val principal = authentication.principal as CustomUserPrincipal

        logger.info("User groups request by user: ${principal.userId}")

        val groups = dashboardService.getUserGroups(principal.institutionId!!)

        return ResponseEntity.ok(groups)
    }

    @GetMapping("/recent-activities")
    @Operation(
        summary = "최근 활동 조회",
        description = "최근 사용자 활동 내역을 조회합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "활동 내역 조회 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "권한 부족")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    fun getRecentActivities(
        @RequestParam(defaultValue = "20") limit: Int,
        @RequestParam(required = false) type: String?,
        authentication: Authentication
    ): ResponseEntity<RecentActivitiesResponse> {
        val principal = authentication.principal as CustomUserPrincipal

        logger.info("Recent activities request by user: ${principal.userId}, limit: $limit, type: $type")

        val activities = dashboardService.getRecentActivities(principal.institutionId!!, limit, type)

        return ResponseEntity.ok(activities)
    }
}
