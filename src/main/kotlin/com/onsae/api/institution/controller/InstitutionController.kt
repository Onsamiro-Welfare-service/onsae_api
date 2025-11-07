package com.onsae.api.institution.controller

import com.onsae.api.institution.dto.*
import com.onsae.api.institution.service.InstitutionService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@Tag(name = "Institution", description = "기관 관련 API")
@RestController
@RequestMapping("/api/institutions")
class InstitutionController(
    private val institutionService: InstitutionService
) {

    @Operation(summary = "기관 목록 조회", description = "활성화된 기관 목록을 조회합니다")
    @GetMapping
    fun getInstitutions(): ResponseEntity<List<InstitutionListResponse>> {
        val institutions = institutionService.getActiveInstitutions()
        return ResponseEntity.ok(institutions)
    }

    @Operation(
        summary = "기관 상세 조회",
        description = "특정 기관의 상세 정보를 조회합니다",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "403", description = "권한 없음"),
        ApiResponse(responseCode = "404", description = "기관을 찾을 수 없음")
    )
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    fun getInstitution(@PathVariable id: Long): ResponseEntity<InstitutionDetailResponse> {
        val institution = institutionService.getInstitution(id)
        return ResponseEntity.ok(institution)
    }

    @Operation(
        summary = "기관 생성",
        description = "새로운 기관을 생성합니다. 시스템 관리자만 접근 가능합니다",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "생성 성공"),
        ApiResponse(responseCode = "400", description = "잘못된 요청"),
        ApiResponse(responseCode = "403", description = "권한 없음"),
        ApiResponse(responseCode = "409", description = "중복된 기관명 또는 사업자등록번호")
    )
    @PostMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    fun createInstitution(@Valid @RequestBody request: InstitutionCreateRequest): ResponseEntity<InstitutionDetailResponse> {
        val institution = institutionService.createInstitution(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(institution)
    }

    @Operation(
        summary = "기관 수정",
        description = "기관 정보를 수정합니다. 시스템 관리자만 접근 가능합니다",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "수정 성공"),
        ApiResponse(responseCode = "400", description = "잘못된 요청"),
        ApiResponse(responseCode = "403", description = "권한 없음"),
        ApiResponse(responseCode = "404", description = "기관을 찾을 수 없음"),
        ApiResponse(responseCode = "409", description = "중복된 기관명 또는 사업자등록번호")
    )
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    fun updateInstitution(
        @PathVariable id: Long,
        @Valid @RequestBody request: InstitutionUpdateRequest
    ): ResponseEntity<InstitutionDetailResponse> {
        val institution = institutionService.updateInstitution(id, request)
        return ResponseEntity.ok(institution)
    }

    @Operation(
        summary = "기관 삭제",
        description = "기관을 삭제합니다(비활성화). 시스템 관리자만 접근 가능합니다",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "삭제 성공"),
        ApiResponse(responseCode = "403", description = "권한 없음"),
        ApiResponse(responseCode = "404", description = "기관을 찾을 수 없음"),
        ApiResponse(responseCode = "409", description = "기관에 연결된 관리자나 사용자가 있어 삭제할 수 없음")
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    fun deleteInstitution(@PathVariable id: Long): ResponseEntity<Void> {
        institutionService.deleteInstitution(id)
        return ResponseEntity.noContent().build()
    }
}