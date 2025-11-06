package com.onsae.api.file.controller

import com.onsae.api.auth.security.CustomUserPrincipal
import com.onsae.api.file.dto.AdminResponseRequest
import com.onsae.api.file.dto.UploadListResponse
import com.onsae.api.file.dto.UploadResponse
import com.onsae.api.file.service.UploadService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

/**
 * 업로드 관리 컨트롤러 (관리자용)
 * 
 * 관리자가 소속 기관의 모든 업로드를 조회하고 응답하는 API를 제공합니다.
 * 
 * 주요 기능:
 * - 기관 전체 업로드 목록 조회: GET /api/admin/uploads
 * - 업로드 상세 조회: GET /api/admin/uploads/{id}
 * - 관리자 응답: PUT /api/admin/uploads/{id}/response
 */
private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/api/admin/uploads")
@Tag(name = "업로드 (관리자)", description = "관리자 업로드 관리 관련 API")
class AdminUploadController(
    private val uploadService: UploadService
) {

    /**
     * 기관의 모든 업로드 목록을 조회합니다.
     * 
     * @param authentication 인증 정보
     * @return 업로드 목록
     */
    @GetMapping
    @Operation(
        summary = "업로드 목록 조회 (관리자)",
        description = "소속 기관의 모든 업로드 목록을 조회합니다. 관리자 권한이 필요합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "목록 조회 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "권한 부족")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    fun getAllUploads(authentication: Authentication): ResponseEntity<List<UploadListResponse>> {
        val principal = authentication.principal as CustomUserPrincipal

        logger.info("Admin upload list request by admin: ${principal.userId}, institution: ${principal.institutionId}")

        val uploads = uploadService.getAllUploadsByInstitution(principal.institutionId!!)

        return ResponseEntity.ok(uploads)
    }

    /**
     * 특정 업로드의 상세 정보를 조회합니다 (관리자용).
     * 
     * @param uploadId 업로드 ID
     * @param authentication 인증 정보
     * @return 업로드 상세 정보
     */
    @GetMapping("/{uploadId}")
    @Operation(
        summary = "업로드 상세 조회 (관리자)",
        description = "특정 업로드의 상세 정보를 조회합니다. 같은 기관의 업로드만 조회 가능합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "상세 조회 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "권한 부족 (다른 기관의 업로드)"),
            ApiResponse(responseCode = "404", description = "업로드를 찾을 수 없음")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    fun getUploadById(
        @PathVariable uploadId: Long,
        authentication: Authentication
    ): ResponseEntity<UploadResponse> {
        val principal = authentication.principal as CustomUserPrincipal

        logger.info("Admin upload detail request: $uploadId by admin: ${principal.userId}")

        val upload = uploadService.getUploadByIdForAdmin(uploadId, principal.institutionId!!)

        return ResponseEntity.ok(upload)
    }

    /**
     * 관리자가 업로드에 대해 응답합니다.
     * 
     * @param uploadId 업로드 ID
     * @param request 관리자 응답 요청
     * @param authentication 인증 정보
     * @return 업데이트된 업로드 정보
     */
    @PutMapping("/{uploadId}/response")
    @Operation(
        summary = "관리자 응답",
        description = "관리자가 업로드된 콘텐츠에 대해 응답합니다. 응답 후 관리자 확인 상태가 true로 변경됩니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "응답 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "권한 부족 (다른 기관의 업로드)"),
            ApiResponse(responseCode = "404", description = "업로드를 찾을 수 없음")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    fun respondToUpload(
        @PathVariable uploadId: Long,
        @Valid @RequestBody request: AdminResponseRequest,
        authentication: Authentication
    ): ResponseEntity<UploadResponse> {
        val principal = authentication.principal as CustomUserPrincipal

        logger.info("Admin response to upload: $uploadId by admin: ${principal.userId}")

        val response = uploadService.respondToUpload(
            uploadId = uploadId,
            request = request,
            adminId = principal.userId,
            institutionId = principal.institutionId!!
        )

        logger.info("Admin response saved successfully")

        return ResponseEntity.ok(response)
    }
}

