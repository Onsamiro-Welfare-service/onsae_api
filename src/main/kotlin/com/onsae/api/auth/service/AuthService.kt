package com.onsae.api.auth.service

import com.onsae.api.auth.dto.*
import com.onsae.api.auth.exception.*
import com.onsae.api.auth.security.JwtTokenProvider
import mu.KotlinLogging
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneId

private val logger = KotlinLogging.logger {}

@Service
@Transactional
class AuthService(
    private val jwtTokenProvider: JwtTokenProvider,
    private val passwordEncoder: PasswordEncoder
) {


    fun login(request: LoginRequestBase): LoginResponse {
        return when (request) {
            is SystemAdminLoginRequest -> loginSystemAdmin(request)
            is AdminLoginRequest -> loginAdmin(request)
            is UserLoginRequest -> loginUser(request)
        }
    }


    private fun loginSystemAdmin(request: SystemAdminLoginRequest): LoginResponse {
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


    private fun loginAdmin(request: AdminLoginRequest): LoginResponse {
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


    private fun loginUser(request: UserLoginRequest): LoginResponse {
        logger.info("User login attempt with code: ${request.loginCode}")

        // 임시 하드코딩된 사용자 계정
        if (request.loginCode == "USER001") {
            val authorities = listOf("ROLE_USER")
            val accessToken = jwtTokenProvider.generateAccessToken(
                userId = 3L,
                userType = "USER",
                institutionId = 1L,
                authorities = authorities
            )
            val refreshToken = jwtTokenProvider.generateRefreshToken(3L)
            val expiresAt = LocalDateTime.now().plusHours(24)

            return LoginResponse(
                accessToken = accessToken,
                refreshToken = refreshToken,
                expiresAt = expiresAt,
                user = UserInfo(
                    id = 3L,
                    userType = "USER",
                    name = "홍길동",
                    email = null,
                    institutionId = 1L,
                    institutionName = "온새 복지관",
                    authorities = authorities
                )
            )
        }

        throw InvalidCredentialsException("올바르지 않은 로그인 코드입니다")
    }

    fun refreshToken(request: RefreshTokenRequest): TokenResponse {
        if (!jwtTokenProvider.validateToken(request.refreshToken)) {
            throw InvalidTokenException("유효하지 않은 리프레시 토큰입니다")
        }

        val userId = jwtTokenProvider.getUserIdFromToken(request.refreshToken)

        // TODO: 실제 사용자 정보 조회 로직 구현
        // 임시로 하드코딩된 정보 사용
        val userType = "ADMIN"
        val institutionId = 1L
        val authorities = listOf("ROLE_ADMIN")

        val newAccessToken = jwtTokenProvider.generateAccessToken(
            userId = userId,
            userType = userType,
            institutionId = institutionId,
            authorities = authorities
        )

        val expiresAt = LocalDateTime.ofInstant(
            jwtTokenProvider.getExpirationDateFromToken(newAccessToken).toInstant(),
            ZoneId.systemDefault()
        )

        return TokenResponse(
            accessToken = newAccessToken,
            expiresAt = expiresAt
        )
    }
}