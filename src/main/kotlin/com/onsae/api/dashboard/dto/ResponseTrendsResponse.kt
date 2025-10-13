package com.onsae.api.dashboard.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "응답 추이 응답")
data class ResponseTrendsResponse(
    @Schema(description = "조회 기간", example = "7d")
    val period: String,

    @Schema(description = "일별 데이터")
    val data: List<DailyResponseData>,

    @Schema(description = "요약 정보")
    val summary: TrendSummary
)

@Schema(description = "일별 응답 데이터")
data class DailyResponseData(
    @Schema(description = "날짜")
    val date: LocalDate,

    @Schema(description = "총 응답 수")
    val totalResponses: Int,

    @Schema(description = "완료된 응답 수")
    val completedResponses: Int,

    @Schema(description = "응답률(%)")
    val responseRate: Double,

    @Schema(description = "카테고리별 응답 수")
    val byCategory: Map<String, Int>
)

@Schema(description = "추이 요약")
data class TrendSummary(
    @Schema(description = "평균 응답률(%)")
    val avgResponseRate: Double,

    @Schema(description = "총 응답 수")
    val totalResponses: Int,

    @Schema(description = "추세", example = "up")
    val trend: String
)
