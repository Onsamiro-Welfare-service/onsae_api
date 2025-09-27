package com.onsae.api.system.controller

import com.onsae.api.auth.dto.LoginResponse
import com.onsae.api.system.dto.SystemLoginRequest
import com.onsae.api.system.service.SystemService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/api/system")
@Tag(name = "시스템 관리", description = "시스템 관리자 전용 API")
class SystemController(
    private val systemService: SystemService
) {

    @PostMapping("/login")
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
    fun login(@Valid @RequestBody request: SystemLoginRequest): ResponseEntity<LoginResponse> {
        logger.info("System admin login attempt - Email: ${request.email}")

        val response = systemService.login(request)

        logger.info("System admin login successful - UserId: ${response.user.id}")

        return ResponseEntity.ok(response)
    }
}