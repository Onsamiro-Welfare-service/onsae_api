package com.onsae.api.user.service

import com.onsae.api.auth.dto.LoginResponse
import com.onsae.api.auth.dto.UserInfo
import com.onsae.api.auth.exception.InvalidCredentialsException
import com.onsae.api.auth.security.JwtTokenProvider
import com.onsae.api.user.dto.UserLoginRequest
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

@Service
@Transactional
class UserService(
    private val jwtTokenProvider: JwtTokenProvider
) {

    fun login(request: UserLoginRequest): LoginResponse {
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
}