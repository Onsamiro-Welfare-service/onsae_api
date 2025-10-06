package com.onsae.api.admin.service

import com.onsae.api.admin.dto.AdminLoginRequest
import com.onsae.api.admin.repository.AdminRepository
import com.onsae.api.auth.dto.LoginResponse
import com.onsae.api.auth.dto.UserInfo
import com.onsae.api.auth.exception.InvalidCredentialsException
import com.onsae.api.auth.security.JwtTokenProvider
import com.onsae.api.common.entity.AccountStatus
import com.onsae.api.institution.repository.InstitutionRepository
import mu.KotlinLogging
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

@Service
@Transactional
class AdminService(
    private val jwtTokenProvider: JwtTokenProvider,
    private val adminRepository: AdminRepository,
    private val institutionRepository: InstitutionRepository,
    private val passwordEncoder: PasswordEncoder
) {

    fun login(request: AdminLoginRequest): LoginResponse {
        logger.info("Admin login attempt for email: ${request.email}, institutionId: ${request.institutionId}")

        // Institution 검증
        val institution = institutionRepository.findById(request.institutionId)
            .orElseThrow { InvalidCredentialsException("존재하지 않는 기관입니다") }

        if (!institution.isActive) {
            throw InvalidCredentialsException("비활성화된 기관입니다")
        }

        // Admin 검증
        val admin = adminRepository.findByInstitutionIdAndEmail(request.institutionId, request.email)
            .orElseThrow { InvalidCredentialsException("관리자 로그인 정보가 올바르지 않습니다") }

        if (!passwordEncoder.matches(request.password, admin.password)) {
            throw InvalidCredentialsException("관리자 로그인 정보가 올바르지 않습니다")
        }

        if (admin.status != AccountStatus.APPROVED) {
            throw InvalidCredentialsException("승인되지 않은 계정입니다")
        }

        if (!admin.isActive) {
            throw InvalidCredentialsException("비활성화된 계정입니다")
        }

        // 로그인 시간 업데이트
        admin.lastLogin = LocalDateTime.now()
        adminRepository.save(admin)

        val authorities = listOf("ROLE_ADMIN")
        val accessToken = jwtTokenProvider.generateAccessToken(
            userId = admin.id!!,
            userType = "ADMIN",
            institutionId = request.institutionId,
            authorities = authorities
        )
        val refreshToken = jwtTokenProvider.generateRefreshToken(admin.id!!)
        val expiresAt = LocalDateTime.now().plusHours(24)

        logger.info("Admin login successful for email: ${request.email}, institutionId: ${request.institutionId}")

        return LoginResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresAt = expiresAt,
            user = UserInfo(
                id = admin.id!!,
                userType = "ADMIN",
                name = admin.name,
                email = admin.email,
                institutionId = request.institutionId,
                institutionName = institution.name,
                authorities = authorities
            )
        )
    }
}