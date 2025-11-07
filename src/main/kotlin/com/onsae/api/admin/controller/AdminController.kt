package com.onsae.api.admin.controller

import com.onsae.api.admin.dto.*
import com.onsae.api.admin.service.AdminRegistrationService
import com.onsae.api.admin.service.AdminService
import com.onsae.api.auth.dto.LoginResponse
import com.onsae.api.auth.security.CustomUserPrincipal
import com.onsae.api.common.entity.AccountStatus
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/api/admin")
@Tag(name = "관리자", description = "복지관 관리자 관련 API")
class AdminController(
    private val adminService: AdminService,
    private val adminRegistrationService: AdminRegistrationService
) {

    @PostMapping("/login")
    @Operation(
        summary = "복지관 관리자 로그인",
        description = "관리자 이메일, 비밀번호, 기관 ID로 로그인합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "로그인 성공"),
            ApiResponse(responseCode = "401", description = "인증 실패"),
            ApiResponse(responseCode = "400", description = "잘못된 요청")
        ]
    )
    @SecurityRequirements
    fun login(@Valid @RequestBody request: AdminLoginRequest): ResponseEntity<LoginResponse> {
        logger.info("Admin login attempt - Email: ${request.email}, InstitutionId: ${request.institutionId}")

        val response = adminService.login(request)

        logger.info("Admin login successful - UserId: ${response.user.id}")

        return ResponseEntity.ok(response)
    }

    @PostMapping("/register")
    @Operation(
        summary = "관리자 회원가입",
        description = "복지관 관리자 또는 직원 회원가입을 진행합니다. 시스템 관리자의 승인이 필요합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "회원가입 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청"),
            ApiResponse(responseCode = "409", description = "이미 등록된 이메일")
        ]
    )
    @SecurityRequirements
    fun register(@Valid @RequestBody request: AdminRegisterRequest): ResponseEntity<AdminRegisterResponse> {
        logger.info("Admin registration attempt for email: ${request.email}")

        val response = adminRegistrationService.registerAdmin(request)

        logger.info("Admin registration successful: adminId=${response.adminId}")

        return ResponseEntity.ok(response)
    }


    @GetMapping
    @Operation(
        summary = "전체 관리자 목록",
        description = """전체 관리자 목록을 조회합니다. 상태별 필터링이 가능합니다.

        Query Parameters:
        - status: 관리자 계정 상태 (PENDING, APPROVED, REJECTED, SUSPENDED)

        시스템 관리자 권한이 필요합니다."""
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "시스템 관리자 권한 필요")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    fun getAllAdmins(
        @RequestParam(required = false) status: AccountStatus?
    ): ResponseEntity<List<AdminListInfo>> {
        logger.info("Fetching all admins with status filter: $status")

        val response = adminRegistrationService.getAllAdmins(status)

        return ResponseEntity.ok(response)
    }

    @PutMapping("/{adminId}/status")
    @Operation(
        summary = "관리자 상태 변경",
        description = """관리자의 상태를 변경합니다.

        - PENDING → APPROVED: 승인 대기 중인 관리자 승인
        - PENDING → REJECTED: 승인 대기 중인 관리자 거부
        - APPROVED → SUSPENDED: 승인된 관리자 정지
        - SUSPENDED → APPROVED: 정지된 관리자 재활성화

        시스템 관리자 권한이 필요합니다."""
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "상태 변경 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "시스템 관리자 권한 필요"),
            ApiResponse(responseCode = "404", description = "관리자를 찾을 수 없음")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    fun changeAdminStatus(
        @PathVariable adminId: Long,
        @Valid @RequestBody request: AdminStatusChangeRequest,
        authentication: Authentication
    ): ResponseEntity<AdminStatusChangeResponse> {
        val principal = authentication.principal as CustomUserPrincipal

        logger.info("Admin status change attempt: adminId=$adminId, status=${request.status}, by=${principal.userId}")

        val response = adminRegistrationService.changeAdminStatus(adminId, request, principal.userId)

        logger.info("Admin status change completed: adminId=$adminId, status=${request.status}")

        return ResponseEntity.ok(response)
    }

    @PutMapping("/approve/{adminId}")
    @Operation(
        summary = "관리자 승인/거부",
        description = "대기 중인 관리자를 승인하거나 거부합니다. 시스템 관리자 권한이 필요합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "처리 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "시스템 관리자 권한 필요"),
            ApiResponse(responseCode = "404", description = "관리자를 찾을 수 없음")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    fun approveAdmin(
        @PathVariable adminId: Long,
        @Valid @RequestBody request: AdminApprovalRequest,
        authentication: Authentication
    ): ResponseEntity<AdminApprovalResponse> {
        val principal = authentication.principal as CustomUserPrincipal

        logger.info("Admin approval attempt: adminId=$adminId, approved=${request.approved}, by=${principal.userId}")

        val response = adminRegistrationService.approveAdmin(adminId, request, principal.userId)

        logger.info("Admin approval completed: adminId=$adminId, approved=${request.approved}")

        return ResponseEntity.ok(response)
    }
}