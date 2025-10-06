package com.onsae.api.survey.controller

import com.onsae.api.auth.security.CustomUserPrincipal
import com.onsae.api.survey.dto.QuestionRequest
import com.onsae.api.survey.dto.QuestionResponse
import com.onsae.api.survey.entity.QuestionType
import com.onsae.api.survey.service.QuestionService
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
@RequestMapping("/api/questions")
@Tag(name = "질문", description = "설문 질문 관리 API")
class QuestionController(
    private val questionService: QuestionService
) {

    @PostMapping
    @Operation(
        summary = "질문 생성",
        description = "새로운 설문 질문을 생성합니다. 관리자 권한이 필요합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "질문 생성 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "권한 부족"),
            ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없음")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    fun createQuestion(
        @Valid @RequestBody request: QuestionRequest,
        authentication: Authentication
    ): ResponseEntity<QuestionResponse> {
        val principal = authentication.principal as CustomUserPrincipal

        logger.info("Question creation request by admin: ${principal.userId}")

        val response = questionService.createQuestion(request, principal.institutionId!!, principal.userId)

        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping
    @Operation(
        summary = "질문 목록 조회",
        description = """소속 기관의 질문 목록을 조회합니다.
        - categoryId: 특정 카테고리 질문만 조회
        - uncategorized=true: 무카테고리 질문만 조회
        - 둘 다 없으면: 전체 질문 조회"""
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "질문 목록 조회 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "권한 부족")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    fun getAllQuestions(
        @RequestParam(required = false) categoryId: Long?,
        @RequestParam(required = false, defaultValue = "false") uncategorized: Boolean,
        authentication: Authentication
    ): ResponseEntity<List<QuestionResponse>> {
        val principal = authentication.principal as CustomUserPrincipal
        val questions = questionService.getAllQuestions(principal.institutionId!!, categoryId, uncategorized)
        return ResponseEntity.ok(questions)
    }

    @GetMapping("/active")
    @Operation(
        summary = "활성 질문 목록 조회",
        description = """소속 기관의 활성 상태인 질문 목록을 조회합니다.
        - categoryId: 특정 카테고리 질문만 조회
        - uncategorized=true: 무카테고리 질문만 조회
        - 둘 다 없으면: 전체 활성 질문 조회"""
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "활성 질문 목록 조회 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "권한 부족")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    fun getActiveQuestions(
        @RequestParam(required = false) categoryId: Long?,
        @RequestParam(required = false, defaultValue = "false") uncategorized: Boolean,
        authentication: Authentication
    ): ResponseEntity<List<QuestionResponse>> {
        val principal = authentication.principal as CustomUserPrincipal
        val questions = questionService.getActiveQuestions(principal.institutionId!!, categoryId, uncategorized)
        return ResponseEntity.ok(questions)
    }

    @GetMapping("/by-type/{questionType}")
    @Operation(
        summary = "질문 유형별 조회",
        description = "특정 유형의 질문들을 조회합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "질문 조회 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "권한 부족")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    fun getQuestionsByType(
        @PathVariable questionType: QuestionType,
        authentication: Authentication
    ): ResponseEntity<List<QuestionResponse>> {
        val principal = authentication.principal as CustomUserPrincipal
        val questions = questionService.getQuestionsByType(principal.institutionId!!, questionType)
        return ResponseEntity.ok(questions)
    }

    @GetMapping("/{questionId}")
    @Operation(
        summary = "질문 상세 조회",
        description = "특정 질문의 상세 정보를 조회합니다."
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
    fun getQuestionById(
        @PathVariable questionId: Long,
        authentication: Authentication
    ): ResponseEntity<QuestionResponse> {
        val principal = authentication.principal as CustomUserPrincipal
        val question = questionService.getQuestionById(questionId, principal.institutionId!!)
        return ResponseEntity.ok(question)
    }

    @PutMapping("/{questionId}")
    @Operation(
        summary = "질문 수정",
        description = "기존 질문의 정보를 수정합니다. 관리자 권한이 필요합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "질문 수정 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "권한 부족"),
            ApiResponse(responseCode = "404", description = "질문을 찾을 수 없음")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    fun updateQuestion(
        @PathVariable questionId: Long,
        @Valid @RequestBody request: QuestionRequest,
        authentication: Authentication
    ): ResponseEntity<QuestionResponse> {
        val principal = authentication.principal as CustomUserPrincipal

        logger.info("Question update request by admin: ${principal.userId} for question: $questionId")

        val response = questionService.updateQuestion(questionId, request, principal.institutionId!!)

        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{questionId}")
    @Operation(
        summary = "질문 삭제",
        description = "질문을 비활성화합니다. 관리자 권한이 필요합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "질문 삭제 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "권한 부족"),
            ApiResponse(responseCode = "404", description = "질문을 찾을 수 없음")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    fun deleteQuestion(
        @PathVariable questionId: Long,
        authentication: Authentication
    ): ResponseEntity<Void> {
        val principal = authentication.principal as CustomUserPrincipal

        logger.info("Question deletion request by admin: ${principal.userId} for question: $questionId")

        questionService.deleteQuestion(questionId, principal.institutionId!!)

        return ResponseEntity.noContent().build()
    }

    @GetMapping("/statistics")
    @Operation(
        summary = "질문 통계 조회",
        description = "소속 기관의 질문 관련 통계를 조회합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "통계 조회 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "권한 부족")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    fun getQuestionStatistics(authentication: Authentication): ResponseEntity<Map<String, Any>> {
        val principal = authentication.principal as CustomUserPrincipal
        val statistics = questionService.getQuestionStatistics(principal.institutionId!!)
        return ResponseEntity.ok(statistics)
    }
}