package com.onsae.api.auth.controller

import com.onsae.api.auth.dto.RefreshTokenRequest
import com.onsae.api.auth.dto.TokenResponse
import com.onsae.api.auth.service.TokenService
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
@RequestMapping("/api/auth")
@Tag(name = "공통 인증", description = "토큰 관리 등 공통 인증 관련 API")
class AuthController(
    private val tokenService: TokenService
) {

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

        val response = tokenService.refreshToken(request)

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
}