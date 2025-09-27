package com.onsae.api.admin.service

import com.onsae.api.admin.dto.AdminLoginRequest
import com.onsae.api.auth.dto.LoginResponse
import com.onsae.api.auth.dto.UserInfo
import com.onsae.api.auth.exception.InvalidCredentialsException
import com.onsae.api.auth.security.JwtTokenProvider
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

@Service
@Transactional
class AdminService(
    private val jwtTokenProvider: JwtTokenProvider
) {

    fun login(request: AdminLoginRequest): LoginResponse {
        logger.info("Admin login attempt for email: ${request.email}, institutionId: ${request.institutionId}")

        // 임시 하드코딩된 관리자 계정
        if (request.email == "admin@example.com" && request.password == "password123" && request.institutionId == 1L) {
            val authorities = listOf("ROLE_ADMIN")
            val accessToken = jwtTokenProvider.generateAccessToken(
                userId = 2L,
                userType = "ADMIN",
                institutionId = request.institutionId,
                authorities = authorities
            )
            val refreshToken = jwtTokenProvider.generateRefreshToken(2L)
            val expiresAt = LocalDateTime.now().plusHours(24)

            return LoginResponse(
                accessToken = accessToken,
                refreshToken = refreshToken,
                expiresAt = expiresAt,
                user = UserInfo(
                    id = 2L,
                    userType = "ADMIN",
                    name = "복지관 관리자",
                    email = "admin@example.com",
                    institutionId = request.institutionId,
                    institutionName = "온새 복지관",
                    authorities = authorities
                )
            )
        }

        throw InvalidCredentialsException("관리자 로그인 정보가 올바르지 않습니다")
    }
}