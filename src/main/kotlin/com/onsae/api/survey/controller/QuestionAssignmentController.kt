package com.onsae.api.survey.controller

import com.onsae.api.auth.security.CustomUserPrincipal
import com.onsae.api.survey.dto.QuestionAssignmentRequest
import com.onsae.api.survey.dto.QuestionAssignmentResponse
import com.onsae.api.survey.service.QuestionAssignmentService
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
@RequestMapping("/api/question-assignments")
@Tag(name = "질문 할당", description = "질문 할당 관리 API")
class QuestionAssignmentController(
    private val questionAssignmentService: QuestionAssignmentService
) {

    @PostMapping
    @Operation(
        summary = "질문 할당",
        description = "사용자 또는 사용자 그룹에게 질문을 할당합니다. 관리자 권한이 필요합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "질문 할당 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "권한 부족"),
            ApiResponse(responseCode = "404", description = "질문 또는 대상을 찾을 수 없음")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    fun createAssignment(
        @Valid @RequestBody request: QuestionAssignmentRequest,
        authentication: Authentication
    ): ResponseEntity<QuestionAssignmentResponse> {
        val principal = authentication.principal as CustomUserPrincipal

        logger.info("Question assignment request by admin: ${principal.userId}")

        val response = questionAssignmentService.createAssignment(request, principal.institutionId!!, principal.userId)

        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping
    @Operation(
        summary = "질문 할당 목록 조회",
        description = "소속 기관의 모든 질문 할당을 조회합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "질문 할당 목록 조회 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "권한 부족")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    fun getAllAssignments(authentication: Authentication): ResponseEntity<List<QuestionAssignmentResponse>> {
        val principal = authentication.principal as CustomUserPrincipal
        val assignments = questionAssignmentService.getAllAssignments(principal.institutionId!!)
        return ResponseEntity.ok(assignments)
    }

    @GetMapping("/{assignmentId}")
    @Operation(
        summary = "질문 할당 상세 조회",
        description = "특정 질문 할당의 상세 정보를 조회합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "질문 할당 조회 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "권한 부족"),
            ApiResponse(responseCode = "404", description = "질문 할당을 찾을 수 없음")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    fun getAssignmentById(
        @PathVariable assignmentId: Long,
        authentication: Authentication
    ): ResponseEntity<QuestionAssignmentResponse> {
        val principal = authentication.principal as CustomUserPrincipal
        val assignment = questionAssignmentService.getAssignmentById(assignmentId, principal.institutionId!!)
        return ResponseEntity.ok(assignment)
    }

    @GetMapping("/by-user/{userId}")
    @Operation(
        summary = "사용자별 질문 할당 조회",
        description = "특정 사용자에게 할당된 질문 목록을 조회합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "사용자별 질문 할당 조회 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "권한 부족"),
            ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    fun getAssignmentsByUser(
        @PathVariable userId: Long,
        authentication: Authentication
    ): ResponseEntity<List<QuestionAssignmentResponse>> {
        val principal = authentication.principal as CustomUserPrincipal
        val assignments = questionAssignmentService.getAssignmentsByUser(userId, principal.institutionId!!)
        return ResponseEntity.ok(assignments)
    }

    @GetMapping("/by-group/{groupId}")
    @Operation(
        summary = "그룹별 질문 할당 조회",
        description = "특정 사용자 그룹에게 할당된 질문 목록을 조회합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "그룹별 질문 할당 조회 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "권한 부족"),
            ApiResponse(responseCode = "404", description = "사용자 그룹을 찾을 수 없음")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    fun getAssignmentsByGroup(
        @PathVariable groupId: Long,
        authentication: Authentication
    ): ResponseEntity<List<QuestionAssignmentResponse>> {
        val principal = authentication.principal as CustomUserPrincipal
        val assignments = questionAssignmentService.getAssignmentsByGroup(groupId, principal.institutionId!!)
        return ResponseEntity.ok(assignments)
    }

    @PutMapping("/{assignmentId}")
    @Operation(
        summary = "질문 할당 수정",
        description = "기존 질문 할당의 정보를 수정합니다. 관리자 권한이 필요합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "질문 할당 수정 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "권한 부족"),
            ApiResponse(responseCode = "404", description = "질문 할당을 찾을 수 없음")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    fun updateAssignment(
        @PathVariable assignmentId: Long,
        @Valid @RequestBody request: QuestionAssignmentRequest,
        authentication: Authentication
    ): ResponseEntity<QuestionAssignmentResponse> {
        val principal = authentication.principal as CustomUserPrincipal

        logger.info("Question assignment update request by admin: ${principal.userId} for assignment: $assignmentId")

        val response = questionAssignmentService.updateAssignment(assignmentId, request, principal.institutionId!!)

        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{assignmentId}")
    @Operation(
        summary = "질문 할당 삭제",
        description = "질문 할당을 삭제합니다. 관리자 권한이 필요합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "질문 할당 삭제 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "권한 부족"),
            ApiResponse(responseCode = "404", description = "질문 할당을 찾을 수 없음")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    fun deleteAssignment(
        @PathVariable assignmentId: Long,
        authentication: Authentication
    ): ResponseEntity<Void> {
        val principal = authentication.principal as CustomUserPrincipal

        logger.info("Question assignment deletion request by admin: ${principal.userId} for assignment: $assignmentId")

        questionAssignmentService.deleteAssignment(assignmentId, principal.institutionId!!)

        return ResponseEntity.noContent().build()
    }

    @GetMapping("/statistics")
    @Operation(
        summary = "질문 할당 통계 조회",
        description = "소속 기관의 질문 할당 관련 통계를 조회합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "통계 조회 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "권한 부족")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    fun getAssignmentStatistics(authentication: Authentication): ResponseEntity<Map<String, Any>> {
        val principal = authentication.principal as CustomUserPrincipal
        val statistics = questionAssignmentService.getAssignmentStatistics(principal.institutionId!!)
        return ResponseEntity.ok(statistics)
    }
}