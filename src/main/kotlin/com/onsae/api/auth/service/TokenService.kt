package com.onsae.api.auth.service

import com.onsae.api.auth.dto.RefreshTokenRequest
import com.onsae.api.auth.dto.TokenResponse
import com.onsae.api.auth.exception.InvalidTokenException
import com.onsae.api.auth.security.JwtTokenProvider
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneId

private val logger = KotlinLogging.logger {}

@Service
@Transactional
class TokenService(
    private val jwtTokenProvider: JwtTokenProvider
) {

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