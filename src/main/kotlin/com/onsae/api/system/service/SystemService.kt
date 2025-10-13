package com.onsae.api.system.service

import com.onsae.api.auth.dto.LoginResponse
import com.onsae.api.auth.dto.UserInfo
import com.onsae.api.auth.exception.InvalidCredentialsException
import com.onsae.api.auth.security.JwtTokenProvider
import com.onsae.api.common.exception.DuplicateException
import com.onsae.api.system.dto.SystemLoginRequest
import com.onsae.api.system.dto.SystemRegisterRequest
import com.onsae.api.system.dto.SystemRegisterResponse
import com.onsae.api.system.entity.SystemAdmin
import com.onsae.api.system.repository.SystemAdminRepository
import mu.KotlinLogging
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

@Service
@Transactional
class SystemService(
    private val jwtTokenProvider: JwtTokenProvider,
    private val systemAdminRepository: SystemAdminRepository,
    private val passwordEncoder: PasswordEncoder
) {

    fun login(request: SystemLoginRequest): LoginResponse {
        logger.info("System admin login attempt for email: ${request.email}")
        logger.debug("Input password: ${request.password}")

        // 데이터베이스에서 사용자 조회 시도
        logger.info("Attempting to find system admin by email: ${request.email}")
        val systemAdminOptional = systemAdminRepository.findByEmail(request.email)

        if (systemAdminOptional.isEmpty) {
            logger.warn("System admin not found for email: ${request.email}")
            throw InvalidCredentialsException("시스템 관리자 로그인 정보가 올바르지 않습니다")
        }

        val systemAdmin = systemAdminOptional.get()
        logger.info("System admin found - ID: ${systemAdmin.id}, Email: ${systemAdmin.email}, Name: ${systemAdmin.name}")
        logger.info("Stored password hash: ${systemAdmin.password}")
        logger.info("Is active: ${systemAdmin.isActive}")
        logger.info("Created at: ${systemAdmin.createdAt}")
        logger.info("Last login: ${systemAdmin.lastLogin}")

        // 비밀번호 검증
        logger.info("Verifying password...")
        val passwordMatches = passwordEncoder.matches(request.password, systemAdmin.password)
        logger.info("Password verification result: $passwordMatches")

        if (!passwordMatches) {
            logger.warn("Password mismatch for email: ${request.email}")
            throw InvalidCredentialsException("시스템 관리자 로그인 정보가 올바르지 않습니다")
        }

        if (!systemAdmin.isActive) {
            throw InvalidCredentialsException("비활성화된 계정입니다")
        }

        // 로그인 시간 업데이트
        systemAdmin.lastLogin = LocalDateTime.now()
        systemAdminRepository.save(systemAdmin)

        val authorities = listOf("ROLE_SYSTEM_ADMIN")
        val accessToken = jwtTokenProvider.generateAccessToken(
            userId = systemAdmin.id!!,
            userType = "SYSTEM_ADMIN",
            institutionId = null,
            authorities = authorities
        )
        val refreshToken = jwtTokenProvider.generateRefreshToken(systemAdmin.id!!)
        val expiresAt = LocalDateTime.now().plusHours(24)

        logger.info("System admin login successful for email: ${request.email}")

        return LoginResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresAt = expiresAt,
            user = UserInfo(
                id = systemAdmin.id!!,
                userType = "SYSTEM_ADMIN",
                name = systemAdmin.name,
                email = systemAdmin.email,
                institutionId = null,
                institutionName = null,
                authorities = authorities
            )
        )
    }

    @Transactional
    fun register(request: SystemRegisterRequest): SystemRegisterResponse {
        logger.info("System admin registration attempt for email: ${request.email}")

        // 중복 이메일 검사
        if (systemAdminRepository.existsByEmail(request.email)) {
            throw DuplicateException("이미 등록된 이메일입니다: ${request.email}")
        }

        // 시스템 관리자 생성
        val systemAdmin = SystemAdmin().apply {
            name = request.name
            email = request.email
            password = passwordEncoder.encode(request.password)
            isActive = true
        }

        val savedSystemAdmin = systemAdminRepository.save(systemAdmin)
        logger.info("System admin registered successfully: ${savedSystemAdmin.id}")

        return SystemRegisterResponse(
            id = savedSystemAdmin.id!!,
            name = savedSystemAdmin.name,
            email = savedSystemAdmin.email,
            isActive = savedSystemAdmin.isActive,
            createdAt = savedSystemAdmin.createdAt
        )
    }
}