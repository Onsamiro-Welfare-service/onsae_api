package com.onsae.api.user.controller

import com.onsae.api.auth.dto.LoginResponse
import com.onsae.api.auth.security.CustomUserPrincipal
import com.onsae.api.user.dto.UserLoginRequest
import com.onsae.api.user.dto.UserRegisterRequest
import com.onsae.api.user.dto.UserRegisterResponse
import com.onsae.api.user.dto.UserProfileResponse
import com.onsae.api.user.dto.UserUpdateRequest
import com.onsae.api.user.dto.UserListResponse
import com.onsae.api.user.service.UserRegistrationService
import com.onsae.api.user.service.UserService
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
@RequestMapping("/api/user")
@Tag(name = "사용자", description = "일반 사용자 관련 API")
class UserController(
    private val userService: UserService,
    private val userRegistrationService: UserRegistrationService,
    private val userSignupService: com.onsae.api.user.service.UserSignupService
) {

    @PostMapping("/login")
    @Operation(
        summary = "일반 사용자 로그인",
        description = "사용자명과 비밀번호로 로그인합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "로그인 성공"),
            ApiResponse(responseCode = "401", description = "인증 실패"),
            ApiResponse(responseCode = "400", description = "잘못된 요청")
        ]
    )
    @SecurityRequirements
    fun login(@Valid @RequestBody request: UserLoginRequest): ResponseEntity<LoginResponse> {
        logger.info("User login attempt - Username: ${request.username}")

        val response = userService.login(request)

        logger.info("User login successful - UserId: ${response.user.id}")

        return ResponseEntity.ok(response)
    }

    @PostMapping("/register")
    @Operation(
        summary = "사용자 등록 (관리자용)",
        description = "관리자가 일반 사용자를 등록합니다. 관리자 권한이 필요합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "사용자 등록 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "권한 부족"),
            ApiResponse(responseCode = "409", description = "이미 등록된 사용자명")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    fun register(
        @Valid @RequestBody request: UserRegisterRequest,
        authentication: Authentication
    ): ResponseEntity<UserRegisterResponse> {
        val principal = authentication.principal as CustomUserPrincipal

        logger.info("User registration attempt by admin: ${principal.userId}, username: ${request.username}")

        val response = userRegistrationService.registerUser(request, principal.userId)

        logger.info("User registration successful: userId=${response.userId}")

        return ResponseEntity.ok(response)
    }

    @PostMapping("/signup")
    @Operation(
        summary = "사용자 회원가입",
        description = "일반 사용자가 직접 회원가입합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "회원가입 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청"),
            ApiResponse(responseCode = "404", description = "기관을 찾을 수 없음"),
            ApiResponse(responseCode = "409", description = "이미 등록된 사용자명")
        ]
    )
    @SecurityRequirements
    fun signup(@Valid @RequestBody request: com.onsae.api.user.dto.UserSignupRequest): ResponseEntity<com.onsae.api.user.dto.UserSignupResponse> {
        logger.info("User signup attempt - username: ${request.username}, institutionId: ${request.institutionId}")

        val response = userSignupService.signup(request)

        logger.info("User signup successful: userId=${response.userId}")

        return ResponseEntity.ok(response)
    }

    @GetMapping("/profile")
    @Operation(
        summary = "사용자 프로필 조회",
        description = "현재 로그인한 사용자의 프로필 정보를 조회합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "프로필 조회 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    fun getProfile(authentication: Authentication): ResponseEntity<UserProfileResponse> {
        val principal = authentication.principal as CustomUserPrincipal
        val profile = userService.getUserProfile(principal.userId)
        return ResponseEntity.ok(profile)
    }

    @GetMapping("/{userId}/profile")
    @Operation(
        summary = "특정 사용자 프로필 조회",
        description = "관리자가 특정 사용자의 프로필 정보를 조회합니다. 관리자 권한이 필요합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "프로필 조회 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "권한 부족"),
            ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    fun getUserProfile(
        @PathVariable userId: Long,
        authentication: Authentication
    ): ResponseEntity<UserProfileResponse> {
        val principal = authentication.principal as CustomUserPrincipal
        val profile = userService.getUserProfileByAdmin(userId, principal.institutionId!!)
        return ResponseEntity.ok(profile)
    }

    @PutMapping("/profile")
    @Operation(
        summary = "사용자 프로필 수정",
        description = "현재 로그인한 사용자의 프로필 정보를 수정합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "프로필 수정 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    fun updateProfile(
        @Valid @RequestBody request: UserUpdateRequest,
        authentication: Authentication
    ): ResponseEntity<UserProfileResponse> {
        val principal = authentication.principal as CustomUserPrincipal
        val updatedProfile = userService.updateUserProfile(principal.userId, request)
        return ResponseEntity.ok(updatedProfile)
    }

    @PutMapping("/{userId}/profile")
    @Operation(
        summary = "특정 사용자 프로필 수정",
        description = "관리자가 특정 사용자의 프로필 정보를 수정합니다. 관리자 권한이 필요합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "프로필 수정 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "권한 부족"),
            ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    fun updateUserProfile(
        @PathVariable userId: Long,
        @Valid @RequestBody request: UserUpdateRequest,
        authentication: Authentication
    ): ResponseEntity<UserProfileResponse> {
        val principal = authentication.principal as CustomUserPrincipal
        val updatedProfile = userService.updateUserProfileByAdmin(userId, request, principal.institutionId!!)
        return ResponseEntity.ok(updatedProfile)
    }

    @GetMapping
    @Operation(
        summary = "사용자 목록 조회",
        description = "관리자가 소속 기관의 모든 사용자 목록을 조회합니다. 관리자 권한이 필요합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "사용자 목록 조회 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "권한 부족")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    fun getAllUsers(authentication: Authentication): ResponseEntity<List<UserListResponse>> {
        val principal = authentication.principal as CustomUserPrincipal
        val users = userService.getAllUsers(principal.institutionId!!)
        return ResponseEntity.ok(users)
    }

    @DeleteMapping("/{userId}")
    @Operation(
        summary = "사용자 삭제 (소프트 삭제)",
        description = "관리자가 특정 사용자를 소프트 삭제합니다. is_active를 false로 설정하여 비활성화합니다. 같은 기관의 사용자만 삭제 가능합니다. 관리자 권한이 필요합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "사용자 삭제 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "권한 부족 (다른 기관의 사용자)"),
            ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    fun deleteUser(
        @PathVariable userId: Long,
        authentication: Authentication
    ): ResponseEntity<Map<String, String>> {
        // 안전한 캐스팅: principal이 CustomUserPrincipal 타입이 아닌 경우 null 반환
        val principal = authentication.principal as? CustomUserPrincipal
            ?: run {
                logger.error("Invalid principal type: ${authentication.principal?.javaClass?.name}")
                return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                    .body(mapOf(
                        "message" to "인증 정보가 올바르지 않습니다",
                        "code" to "INVALID_AUTHENTICATION"
                    ))
            }

        logger.info("User deletion request: $userId by admin: ${principal.userId}, institution: ${principal.institutionId}")

        userService.deleteUser(userId, principal.institutionId!!)

        logger.info("User deleted successfully: $userId")

        return ResponseEntity.ok(mapOf(
            "message" to "사용자가 성공적으로 삭제되었습니다",
            "code" to "USER_DELETED"
        ))
    }
}