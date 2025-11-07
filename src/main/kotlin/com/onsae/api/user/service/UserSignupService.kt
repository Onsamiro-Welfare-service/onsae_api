package com.onsae.api.user.service

import com.onsae.api.common.exception.BusinessException
import com.onsae.api.common.exception.DuplicateException
import com.onsae.api.institution.repository.InstitutionRepository
import com.onsae.api.user.dto.UserSignupRequest
import com.onsae.api.user.dto.UserSignupResponse
import com.onsae.api.user.entity.User
import com.onsae.api.user.repository.UserRepository
import mu.KotlinLogging
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

@Service
@Transactional
class UserSignupService(
    private val userRepository: UserRepository,
    private val institutionRepository: InstitutionRepository,
    private val passwordEncoder: PasswordEncoder
) {

    fun signup(request: UserSignupRequest): UserSignupResponse {
        logger.info("User signup attempt - username: ${request.username}, institutionId: ${request.institutionId}")

        // 기관 존재 여부 확인
        val institution = institutionRepository.findById(request.institutionId)
            .orElseThrow { BusinessException("존재하지 않는 기관입니다", "INSTITUTION_NOT_FOUND") }

        if (!institution.isActive) {
            throw BusinessException("비활성화된 기관입니다", "INSTITUTION_INACTIVE")
        }

        // 사용자명 중복 검사 (기관 내에서)
        if (userRepository.existsByInstitutionIdAndUsername(request.institutionId, request.username)) {
            throw DuplicateException("이미 사용 중인 사용자명입니다: ${request.username}")
        }

        // 사용자 생성
        val user = User().apply {
            this.institution = institution
            this.username = request.username
            this.password = passwordEncoder.encode(request.password)
            this.name = request.name
            this.phone = request.phone
            this.birthDate = request.birthDate
            this.isActive = true
        }

        val savedUser = userRepository.save(user)

        logger.info("User signup successful - userId: ${savedUser.id}, username: ${request.username}")

        return UserSignupResponse(
            userId = savedUser.id!!,
            username = savedUser.username,
            name = savedUser.name,
            institutionId = institution.id!!,
            institutionName = institution.name,
            createdAt = savedUser.createdAt
        )
    }
}
