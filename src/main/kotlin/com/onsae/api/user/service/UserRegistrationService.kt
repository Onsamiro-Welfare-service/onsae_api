package com.onsae.api.user.service

import com.onsae.api.admin.repository.AdminRepository
import com.onsae.api.auth.exception.InvalidCredentialsException
import com.onsae.api.common.entity.AccountStatus
import com.onsae.api.common.exception.BusinessException
import com.onsae.api.common.exception.DuplicateException
import com.onsae.api.institution.repository.InstitutionRepository
import com.onsae.api.user.dto.UserRegisterRequest
import com.onsae.api.user.dto.UserRegisterResponse
import com.onsae.api.user.entity.User
import com.onsae.api.user.repository.UserRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

@Service
@Transactional
class UserRegistrationService(
    private val userRepository: UserRepository,
    private val adminRepository: AdminRepository,
    private val institutionRepository: InstitutionRepository,
    private val temporaryLoginCodeService: TemporaryLoginCodeService
) {

    fun registerUser(request: UserRegisterRequest, adminId: Long): UserRegisterResponse {
        logger.info("User registration attempt by admin: $adminId, loginCode: ${request.loginCode}")

        // Admin 정보 조회 (기관 ID 확인용)
        val adminInfo = getAdminInfo(adminId)
        if (adminInfo.status != "APPROVED") {
            throw InvalidCredentialsException("승인된 관리자만 사용자를 등록할 수 있습니다")
        }

        // 로그인 코드 중복 검사 (기관 내에서)
        if (isLoginCodeExists(request.loginCode, adminInfo.institutionId)) {
            throw DuplicateException("이미 사용 중인 로그인 코드입니다: ${request.loginCode}")
        }

        // 그룹 검증 (해당 기관의 그룹인지 확인)
        if (request.groupId != null) {
            validateUserGroupBelongsToInstitution(request.groupId, adminInfo.institutionId)
        }

        // User 등록 (임시 하드코딩 - 실제 엔티티 구현 후 교체)
        val userId = createUser(
            name = request.name,
            loginCode = request.loginCode,
            phone = request.phone,
            birthDate = request.birthDate,
            institutionId = adminInfo.institutionId,
            groupId = request.groupId
        )

        // 임시 로그인 코드 생성
        val temporaryCode = temporaryLoginCodeService.generateTemporaryCode(userId)

        logger.info("User registered successfully: userId=$userId, loginCode=${request.loginCode}, temporaryCode=$temporaryCode")

        return UserRegisterResponse(
            userId = userId,
            name = request.name,
            loginCode = temporaryCode, // 임시 코드 반환
            institutionId = adminInfo.institutionId,
            institutionName = adminInfo.institutionName,
            createdAt = LocalDateTime.now()
        )
    }

    private fun getAdminInfo(adminId: Long): AdminInfo {
        val admin = adminRepository.findById(adminId)
            .orElseThrow { BusinessException("존재하지 않는 관리자입니다", "ADMIN_NOT_FOUND") }

        return AdminInfo(
            id = admin.id!!,
            institutionId = admin.institution.id!!,
            institutionName = admin.institution.name,
            status = admin.status.name
        )
    }

    private fun isLoginCodeExists(loginCode: String, institutionId: Long): Boolean {
        return userRepository.existsByInstitutionIdAndUsercode(institutionId, loginCode)
    }

    private fun validateUserGroupBelongsToInstitution(groupId: Long, institutionId: Long) {
        // TODO: UserGroupRepository가 있다면 여기서 검증
        // 현재는 기본적인 검증만 수행
        logger.debug("Validating group $groupId belongs to institution $institutionId")
    }

    private fun createUser(
        name: String,
        loginCode: String,
        phone: String?,
        birthDate: java.time.LocalDate?,
        institutionId: Long,
        groupId: Long?
    ): Long {
        logger.info("Creating user: $name, $loginCode")

        val institution = institutionRepository.findById(institutionId)
            .orElseThrow { BusinessException("존재하지 않는 기관입니다", "INSTITUTION_NOT_FOUND") }

        val user = User().apply {
            this.institution = institution
            this.usercode = loginCode
            this.name = name
            this.phone = phone
            this.birthDate = birthDate
        }

        return userRepository.save(user).id!!
    }

    private data class AdminInfo(
        val id: Long,
        val institutionId: Long,
        val institutionName: String,
        val status: String
    )
}