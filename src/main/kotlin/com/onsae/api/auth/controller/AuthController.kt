package com.onsae.api.auth.controller

import com.onsae.api.auth.dto.*
import com.onsae.api.auth.security.CustomUserPrincipal
import com.onsae.api.auth.service.AuthService
import com.onsae.api.auth.service.RegistrationService
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
@RequestMapping("/api/auth")
@Tag(name = "인증", description = "로그인, 토큰 관리 등 인증 관련 API")
class AuthController(
    private val authService: AuthService,
    private val registrationService: RegistrationService
) {


    @PostMapping("/login/system-admin")
    @Operation(
        summary = "시스템 관리자 로그인",
        description = "시스템 관리자 이메일과 비밀번호로 로그인합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "로그인 성공"),
            ApiResponse(responseCode = "401", description = "인증 실패"),
            ApiResponse(responseCode = "400", description = "잘못된 요청")
        ]
    )
    @SecurityRequirements
    fun loginSystemAdmin(@Valid @RequestBody request: SystemAdminLoginRequest): ResponseEntity<LoginResponse> {
        logger.info("System admin login attempt - Email: ${request.email}")

        val response = authService.login(request)

        logger.info("System admin login successful - UserId: ${response.user.id}")

        return ResponseEntity.ok(response)
    }

    @PostMapping("/login/admin")
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
    fun loginAdmin(@Valid @RequestBody request: AdminLoginRequest): ResponseEntity<LoginResponse> {
        logger.info("Admin login attempt - Email: ${request.email}, InstitutionId: ${request.institutionId}")

        val response = authService.login(request)

        logger.info("Admin login successful - UserId: ${response.user.id}")

        return ResponseEntity.ok(response)
    }

    @PostMapping("/login/user")
    @Operation(
        summary = "일반 사용자 로그인",
        description = "사용자 로그인 코드로 로그인합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "로그인 성공"),
            ApiResponse(responseCode = "401", description = "인증 실패"),
            ApiResponse(responseCode = "400", description = "잘못된 요청")
        ]
    )
    @SecurityRequirements
    fun loginUser(@Valid @RequestBody request: UserLoginRequest): ResponseEntity<LoginResponse> {
        logger.info("User login attempt - LoginCode: ${request.loginCode}")

        val response = authService.login(request)

        logger.info("User login successful - UserId: ${response.user.id}")

        return ResponseEntity.ok(response)
    }

    @PostMapping("/refresh")
    @Operation(
        summary = "토큰 갱신",
        description = "리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급받습니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
            ApiResponse(responseCode = "401", description = "유효하지 않은 리프레시 토큰"),
            ApiResponse(responseCode = "400", description = "잘못된 요청")
        ]
    )
    @SecurityRequirements
    fun refreshToken(@Valid @RequestBody request: RefreshTokenRequest): ResponseEntity<TokenResponse> {
        logger.info("Token refresh attempt")

        val response = authService.refreshToken(request)

        logger.info("Token refresh successful")

        return ResponseEntity.ok(response)
    }

    @PostMapping("/logout")
    @Operation(
        summary = "로그아웃",
        description = "현재 세션을 종료합니다. (현재는 클라이언트에서 토큰 삭제로 처리)"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "로그아웃 성공")
        ]
    )
    @SecurityRequirements
    fun logout(): ResponseEntity<Map<String, String>> {
        // 현재는 단순히 성공 응답만 반환
        // 향후 토큰 블랙리스트 또는 Redis 기반 세션 관리 구현 가능
        logger.info("Logout request")

        return ResponseEntity.ok(
            mapOf(
                "message" to "로그아웃이 완료되었습니다.",
                "timestamp" to java.time.LocalDateTime.now().toString()
            )
        )
    }

    @PostMapping("/register/admin")
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
    fun registerAdmin(@Valid @RequestBody request: AdminRegisterRequest): ResponseEntity<AdminRegisterResponse> {
        logger.info("Admin registration attempt for email: ${request.email}")

        val response = registrationService.registerAdmin(request)

        logger.info("Admin registration successful: adminId=${response.adminId}")

        return ResponseEntity.ok(response)
    }

    @PostMapping("/register/user")
    @Operation(
        summary = "사용자 등록",
        description = "관리자가 일반 사용자를 등록합니다. 관리자 권한이 필요합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "사용자 등록 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "권한 부족"),
            ApiResponse(responseCode = "409", description = "이미 등록된 로그인 코드")
        ]
    )
    fun registerUser(
        @Valid @RequestBody request: UserRegisterRequest,
        authentication: Authentication
    ): ResponseEntity<UserRegisterResponse> {
        val principal = authentication.principal as CustomUserPrincipal

        logger.info("User registration attempt by admin: ${principal.userId}, loginCode: ${request.loginCode}")

        val response = registrationService.registerUser(request, principal.userId)

        logger.info("User registration successful: userId=${response.userId}")

        return ResponseEntity.ok(response)
    }

    @GetMapping("/pending-admins")
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

        val response = registrationService.getPendingAdmins()

        return ResponseEntity.ok(response)
    }

    @PutMapping("/approve-admin/{adminId}")
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

        val response = registrationService.approveAdmin(adminId, request, principal.userId)

        logger.info("Admin approval completed: adminId=$adminId, approved=${request.approved}")

        return ResponseEntity.ok(response)
    }
}