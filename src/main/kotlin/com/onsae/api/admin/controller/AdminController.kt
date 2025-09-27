package com.onsae.api.admin.controller

import com.onsae.api.admin.dto.*
import com.onsae.api.admin.service.AdminRegistrationService
import com.onsae.api.admin.service.AdminService
import com.onsae.api.auth.dto.LoginResponse
import com.onsae.api.auth.security.CustomUserPrincipal
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
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

    @GetMapping("/pending")
    @Operation(
        summary = "승인 대기 관리자 목록",
        description = "승인 대기 중인 관리자 목록을 조회합니다. 시스템 관리자 권한이 필요합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "시스템 관리자 권한 필요")
        ]
    )
    fun getPendingAdmins(): ResponseEntity<List<PendingAdminInfo>> {
        logger.info("Fetching pending admins")

        val response = adminRegistrationService.getPendingAdmins()

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