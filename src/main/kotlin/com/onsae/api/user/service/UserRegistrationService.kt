package com.onsae.api.user.service

import com.onsae.api.auth.exception.InvalidCredentialsException
import com.onsae.api.common.exception.BusinessException
import com.onsae.api.common.exception.DuplicateException
import com.onsae.api.user.dto.UserRegisterRequest
import com.onsae.api.user.dto.UserRegisterResponse
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

@Service
@Transactional
class UserRegistrationService {

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

        logger.info("User registered successfully: userId=$userId, loginCode=${request.loginCode}")

        return UserRegisterResponse(
            userId = userId,
            name = request.name,
            loginCode = request.loginCode,
            institutionId = adminInfo.institutionId,
            institutionName = adminInfo.institutionName,
            createdAt = LocalDateTime.now()
        )
    }

    // 임시 메서드들 - 실제 Repository 구현 후 교체 예정
    private fun getAdminInfo(adminId: Long): AdminInfo {
        // TODO: AdminRepository에서 관리자 정보 조회
        return AdminInfo(
            id = adminId,
            institutionId = 1L,
            institutionName = "테스트 복지관",
            status = "APPROVED"
        )
    }

    private fun isLoginCodeExists(loginCode: String, institutionId: Long): Boolean {
        // TODO: UserRepository에서 기관 내 로그인 코드 중복 확인
        return loginCode == "EXISTING001" // 임시 하드코딩
    }

    private fun validateUserGroupBelongsToInstitution(groupId: Long, institutionId: Long) {
        // TODO: UserGroupRepository에서 그룹이 해당 기관에 속하는지 확인
        if (groupId > 100L) { // 임시 하드코딩
            throw BusinessException("해당 기관에 속하지 않는 그룹입니다", "INVALID_GROUP")
        }
    }

    private fun createUser(
        name: String,
        loginCode: String,
        phone: String?,
        birthDate: java.time.LocalDate?,
        institutionId: Long,
        groupId: Long?
    ): Long {
        // TODO: User 엔티티 생성 및 저장
        logger.info("Creating user: $name, $loginCode")
        return System.currentTimeMillis() // 임시 ID 생성
    }

    // 임시 데이터 클래스
    private data class AdminInfo(
        val id: Long,
        val institutionId: Long,
        val institutionName: String,
        val status: String
    )
}