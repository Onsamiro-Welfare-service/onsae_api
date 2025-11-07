package com.onsae.api.user.controller

import com.onsae.api.auth.security.CustomUserPrincipal
import com.onsae.api.survey.dto.QuestionResponseRequest
import com.onsae.api.survey.dto.UserQuestionResponse
import com.onsae.api.survey.service.UserQuestionService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/api/user/questions")
@Tag(name = "사용자 질문", description = "사용자용 질문 조회 및 응답 API")
class UserQuestionController(
    private val userQuestionService: UserQuestionService
) {

    @GetMapping
    @Operation(
        summary = "내 질문 목록 조회",
        description = "나에게 할당된 모든 질문을 조회합니다. 개별 할당과 그룹 할당 질문을 모두 포함합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "질문 목록 조회 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "권한 부족")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    fun getMyQuestions(authentication: Authentication): ResponseEntity<List<UserQuestionResponse>> {
        val principal = authentication.principal as CustomUserPrincipal
        val questions = userQuestionService.getMyQuestions(principal.userId)
        return ResponseEntity.ok(questions)
    }

    @GetMapping("/{assignmentId}")
    @Operation(
        summary = "질문 상세 조회",
        description = "특정 할당 질문의 상세 정보를 조회합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "질문 조회 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "권한 부족"),
            ApiResponse(responseCode = "404", description = "질문을 찾을 수 없음")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    fun getMyQuestionById(
        @PathVariable assignmentId: Long,
        authentication: Authentication
    ): ResponseEntity<UserQuestionResponse> {
        val principal = authentication.principal as CustomUserPrincipal
        val question = userQuestionService.getMyQuestionById(assignmentId, principal.userId)
        return ResponseEntity.ok(question)
    }

    @PostMapping("/responses")
    @Operation(
        summary = "질문 응답 제출",
        description = "할당된 질문에 대한 응답을 제출합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "응답 제출 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "권한 부족"),
            ApiResponse(responseCode = "404", description = "질문을 찾을 수 없음"),
            ApiResponse(responseCode = "409", description = "이미 응답한 질문")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    fun submitResponse(
        @Valid @RequestBody request: QuestionResponseRequest,
        authentication: Authentication
    ): ResponseEntity<UserQuestionResponse> {
        val principal = authentication.principal as CustomUserPrincipal

        logger.info("Question response submission by user: ${principal.userId} for assignment: ${request.assignmentId}")

        val response = userQuestionService.submitResponse(request, principal.userId)

        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping("/pending")
    @Operation(
        summary = "미응답 질문 목록 조회",
        description = "아직 응답하지 않은 질문들만 조회합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "미응답 질문 목록 조회 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "권한 부족")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    fun getPendingQuestions(authentication: Authentication): ResponseEntity<List<UserQuestionResponse>> {
        val principal = authentication.principal as CustomUserPrincipal
        val questions = userQuestionService.getMyQuestions(principal.userId)
            .filter { !it.isCompleted }
        return ResponseEntity.ok(questions)
    }

    @GetMapping("/completed")
    @Operation(
        summary = "응답 완료 질문 목록 조회",
        description = "이미 응답한 질문들만 조회합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "응답 완료 질문 목록 조회 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "권한 부족")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    fun getCompletedQuestions(authentication: Authentication): ResponseEntity<List<UserQuestionResponse>> {
        val principal = authentication.principal as CustomUserPrincipal
        val questions = userQuestionService.getMyQuestions(principal.userId)
            .filter { it.isCompleted }
        return ResponseEntity.ok(questions)
    }

    @GetMapping("/statistics")
    @Operation(
        summary = "내 질문 응답 통계 조회",
        description = "나의 질문 응답 진행 상황 통계를 조회합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "통계 조회 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "권한 부족")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    fun getMyQuestionStatistics(authentication: Authentication): ResponseEntity<Map<String, Any>> {
        val principal = authentication.principal as CustomUserPrincipal
        val statistics = userQuestionService.getMyQuestionStatistics(principal.userId)
        return ResponseEntity.ok(statistics)
    }
}