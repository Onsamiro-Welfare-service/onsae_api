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
import com.onsae.api.user.entity.UserGroupMember
import com.onsae.api.user.repository.UserRepository
import com.onsae.api.user.repository.UserGroupRepository
import com.onsae.api.user.repository.UserGroupMemberRepository
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
    private val temporaryLoginCodeService: TemporaryLoginCodeService,
    private val userGroupRepository: UserGroupRepository,
    private val userGroupMemberRepository: UserGroupMemberRepository
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
        if (!request.groupIds.isNullOrEmpty()) {
            validateUserGroupsBelongToInstitution(request.groupIds, adminInfo.institutionId)
        }

        // User 등록 (임시 하드코딩 - 실제 엔티티 구현 후 교체)
        val userId = createUser(
            name = request.name,
            loginCode = request.loginCode,
            phone = request.phone,
            birthDate = request.birthDate,
            institutionId = adminInfo.institutionId
        )

        // 초기 그룹 멤버십 연결 (옵션)
        if (!request.groupIds.isNullOrEmpty()) {
            addUserToGroups(userId, request.groupIds, adminInfo.institutionId, adminInfo.id)
        }

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
        institutionId: Long
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

    private fun validateUserGroupsBelongToInstitution(groupIds: List<Long>, institutionId: Long) {
        val groups = userGroupRepository.findAllById(groupIds)
        if (groups.size != groupIds.toSet().size) {
            throw BusinessException("존재하지 않는 그룹 ID가 포함되어 있습니다", "GROUP_NOT_FOUND")
        }
        val invalid = groups.firstOrNull { it.institution.id != institutionId }
        if (invalid != null) {
            throw InvalidCredentialsException("다른 기관의 그룹이 포함되어 있습니다: ${invalid.id}")
        }
    }

    private fun addUserToGroups(userId: Long, groupIds: List<Long>, institutionId: Long, adminId: Long) {
        val user = userRepository.findById(userId)
            .orElseThrow { BusinessException("존재하지 않는 사용자입니다", "USER_NOT_FOUND") }
        if (user.institution.id != institutionId) {
            throw InvalidCredentialsException("사용자와 기관이 일치하지 않습니다")
        }

        val admin = adminRepository.findById(adminId).orElse(null)
        val groups = userGroupRepository.findAllById(groupIds)

        groups.forEach { group ->
            val gid = group.id ?: return@forEach
            if (!userGroupMemberRepository.existsByGroupIdAndUserId(gid, userId)) {
                val member = UserGroupMember().apply {
                    this.group = group
                    this.user = user
                    this.addedBy = admin
                }
                userGroupMemberRepository.save(member)

                // 그룹 멤버수 업데이트
                group.memberCount = userGroupMemberRepository.countByGroupIdAndIsActive(gid, true)
                userGroupRepository.save(group)
            }
        }
    }

    private data class AdminInfo(
        val id: Long,
        val institutionId: Long,
        val institutionName: String,
        val status: String
    )
}
