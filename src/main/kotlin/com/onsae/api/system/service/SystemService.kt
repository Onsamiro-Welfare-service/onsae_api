package com.onsae.api.system.service

import com.onsae.api.auth.dto.LoginResponse
import com.onsae.api.auth.dto.UserInfo
import com.onsae.api.auth.exception.InvalidCredentialsException
import com.onsae.api.auth.security.JwtTokenProvider
import com.onsae.api.system.dto.SystemLoginRequest
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

@Service
@Transactional
class SystemService(
    private val jwtTokenProvider: JwtTokenProvider
) {

    fun login(request: SystemLoginRequest): LoginResponse {
        logger.info("System admin login attempt for email: ${request.email}")

        // 임시 하드코딩된 시스템 관리자 계정
        if (request.email == "system@onsae.com" && request.password == "admin123!") {
            val authorities = listOf("ROLE_SYSTEM_ADMIN")
            val accessToken = jwtTokenProvider.generateAccessToken(
                userId = 1L,
                userType = "SYSTEM_ADMIN",
                institutionId = null,
                authorities = authorities
            )
            val refreshToken = jwtTokenProvider.generateRefreshToken(1L)
            val expiresAt = LocalDateTime.now().plusHours(24)

            return LoginResponse(
                accessToken = accessToken,
                refreshToken = refreshToken,
                expiresAt = expiresAt,
                user = UserInfo(
                    id = 1L,
                    userType = "SYSTEM_ADMIN",
                    name = "시스템 관리자",
                    email = "system@onsae.com",
                    institutionId = null,
                    institutionName = null,
                    authorities = authorities
                )
            )
        }

        throw InvalidCredentialsException("시스템 관리자 로그인 정보가 올바르지 않습니다")
    }
}