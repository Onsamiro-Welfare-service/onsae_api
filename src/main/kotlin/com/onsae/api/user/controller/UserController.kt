package com.onsae.api.user.controller

import com.onsae.api.auth.dto.LoginResponse
import com.onsae.api.auth.security.CustomUserPrincipal
import com.onsae.api.user.dto.UserLoginRequest
import com.onsae.api.user.dto.UserRegisterRequest
import com.onsae.api.user.dto.UserRegisterResponse
import com.onsae.api.user.service.UserRegistrationService
import com.onsae.api.user.service.UserService
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
@RequestMapping("/api/user")
@Tag(name = "사용자", description = "일반 사용자 관련 API")
class UserController(
    private val userService: UserService,
    private val userRegistrationService: UserRegistrationService
) {

    @PostMapping("/login")
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
    fun login(@Valid @RequestBody request: UserLoginRequest): ResponseEntity<LoginResponse> {
        logger.info("User login attempt - LoginCode: ${request.loginCode}")

        val response = userService.login(request)

        logger.info("User login successful - UserId: ${response.user.id}")

        return ResponseEntity.ok(response)
    }

    @PostMapping("/register")
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
    fun register(
        @Valid @RequestBody request: UserRegisterRequest,
        authentication: Authentication
    ): ResponseEntity<UserRegisterResponse> {
        val principal = authentication.principal as CustomUserPrincipal

        logger.info("User registration attempt by admin: ${principal.userId}, loginCode: ${request.loginCode}")

        val response = userRegistrationService.registerUser(request, principal.userId)

        logger.info("User registration successful: userId=${response.userId}")

        return ResponseEntity.ok(response)
    }
}