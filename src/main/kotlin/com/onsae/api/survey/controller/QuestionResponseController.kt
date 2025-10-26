package com.onsae.api.survey.controller

import com.onsae.api.auth.security.CustomUserPrincipal
import com.onsae.api.survey.dto.AssignmentResponseSummaryDTO
import com.onsae.api.survey.dto.QuestionResponseDetailDTO
import com.onsae.api.survey.dto.UserResponseSummaryDTO
import com.onsae.api.survey.service.QuestionResponseService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import mu.KotlinLogging
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/api/responses")
@Tag(name = "응답 조회", description = "관리자용 사용자 응답 조회 API")
class QuestionResponseController(
    private val questionResponseService: QuestionResponseService
) {

    @GetMapping("/user/{userId}")
    @Operation(
        summary = "사용자별 응답 조회",
        description = "특정 사용자의 모든 응답을 조회합니다. 매일 반복 응답도 모두 포함됩니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "응답 조회 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "권한 부족"),
            ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    fun getUserResponses(
        @PathVariable userId: Long,
        authentication: Authentication
    ): ResponseEntity<UserResponseSummaryDTO> {
        val principal = authentication.principal as CustomUserPrincipal

        logger.info { "Fetching responses for user: $userId by admin: ${principal.userId}" }

        val responses = questionResponseService.getUserResponses(userId, principal.institutionId!!)

        return ResponseEntity.ok(responses)
    }

    @GetMapping("/assignment/{assignmentId}")
    @Operation(
        summary = "할당별 응답 조회",
        description = "특정 질문 할당에 대한 모든 응답을 조회합니다. 같은 사용자의 매일 반복 응답도 모두 포함됩니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "응답 조회 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "권한 부족"),
            ApiResponse(responseCode = "404", description = "할당을 찾을 수 없음")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    fun getAssignmentResponses(
        @PathVariable assignmentId: Long,
        authentication: Authentication
    ): ResponseEntity<AssignmentResponseSummaryDTO> {
        val principal = authentication.principal as CustomUserPrincipal

        logger.info { "Fetching responses for assignment: $assignmentId by admin: ${principal.userId}" }

        val responses = questionResponseService.getAssignmentResponses(assignmentId, principal.institutionId!!)

        return ResponseEntity.ok(responses)
    }

    @GetMapping("/user/{userId}/date-range")
    @Operation(
        summary = "사용자별 기간 응답 조회",
        description = "특정 사용자의 특정 기간 내 응답을 조회합니다. 날짜별 응답 추이 확인에 유용합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "응답 조회 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "권한 부족"),
            ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    fun getUserResponsesByDateRange(
        @PathVariable userId: Long,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) startDate: LocalDateTime,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) endDate: LocalDateTime,
        authentication: Authentication
    ): ResponseEntity<List<QuestionResponseDetailDTO>> {
        val principal = authentication.principal as CustomUserPrincipal

        logger.info { "Fetching responses for user: $userId between $startDate and $endDate" }

        val responses = questionResponseService.getUserResponsesByDateRange(
            userId,
            principal.institutionId!!,
            startDate,
            endDate
        )

        return ResponseEntity.ok(responses)
    }

    @GetMapping("/recent")
    @Operation(
        summary = "최근 응답 조회",
        description = "기관의 최근 응답을 조회합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "응답 조회 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "권한 부족")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    fun getRecentResponses(
        @RequestParam(defaultValue = "20") limit: Int,
        authentication: Authentication
    ): ResponseEntity<List<QuestionResponseDetailDTO>> {
        val principal = authentication.principal as CustomUserPrincipal

        logger.info { "Fetching recent $limit responses for institution: ${principal.institutionId}" }

        val responses = questionResponseService.getRecentResponses(principal.institutionId!!, limit)

        return ResponseEntity.ok(responses)
    }

    @GetMapping("/question/{questionId}/user/{userId}/history")
    @Operation(
        summary = "질문별 사용자 응답 이력 조회",
        description = "특정 날짜에 특정 사용자가 특정 질문에 답변한 모든 이력을 조회합니다. 같은 날 여러 번 수정한 경우 모든 이력이 포함됩니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "응답 이력 조회 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "권한 부족"),
            ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    fun getQuestionResponseHistory(
        @PathVariable questionId: Long,
        @PathVariable userId: Long,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate,
        authentication: Authentication
    ): ResponseEntity<List<QuestionResponseDetailDTO>> {
        val principal = authentication.principal as CustomUserPrincipal

        logger.info { "Fetching response history for question: $questionId, user: $userId, date: $date" }

        val responses = questionResponseService.getQuestionResponseHistory(
            questionId,
            userId,
            date,
            principal.institutionId!!
        )

        return ResponseEntity.ok(responses)
    }
}
