package com.onsae.api.auth.service

import com.onsae.api.auth.dto.*
import com.onsae.api.auth.exception.InvalidCredentialsException
import com.onsae.api.common.exception.BusinessException
import com.onsae.api.common.exception.DuplicateException
import mu.KotlinLogging
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

@Service
@Transactional
class RegistrationService(
    private val passwordEncoder: PasswordEncoder
) {

    fun registerAdmin(request: AdminRegisterRequest): AdminRegisterResponse {
        logger.info("Admin registration attempt for email: ${request.email}")

        request.validate()

        // 이메일 중복 검사
        if (isEmailExists(request.email)) {
            throw DuplicateException("이미 등록된 이메일입니다: ${request.email}")
        }

        // 기관 처리
        val institutionId = when {
            request.institutionId != null -> {
                // 기존 기관 확인
                validateInstitutionExists(request.institutionId)
                request.institutionId
            }
            request.institutionInfo != null -> {
                // 새 기관 등록
                createInstitution(request.institutionInfo)
            }
            else -> throw IllegalArgumentException("기관 정보가 필요합니다")
        }

        // 비밀번호 암호화
        val encodedPassword = passwordEncoder.encode(request.password)

        // Admin 등록 (임시 하드코딩 - 실제 엔티티 구현 후 교체)
        val adminId = createAdmin(
            name = request.name,
            email = request.email,
            encodedPassword = encodedPassword,
            phone = request.phone,
            role = request.role.name,
            institutionId = institutionId
        )

        val institutionName = getInstitutionName(institutionId)

        logger.info("Admin registered successfully: adminId=$adminId, email=${request.email}")

        return AdminRegisterResponse(
            adminId = adminId,
            name = request.name,
            email = request.email,
            role = request.role.name,
            institutionId = institutionId,
            institutionName = institutionName,
            status = "PENDING",
            createdAt = LocalDateTime.now()
        )
    }

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

    fun getPendingAdmins(): List<PendingAdminInfo> {
        logger.info("Fetching pending admins")

        // 임시 하드코딩 - 실제 Repository 구현 후 교체
        return listOf(
            PendingAdminInfo(
                id = 100L,
                name = "대기중 관리자",
                email = "pending@example.com",
                phone = "010-1234-5678",
                role = "ADMIN",
                institutionId = 1L,
                institutionName = "테스트 복지관",
                createdAt = LocalDateTime.now().minusDays(1)
            )
        )
    }

    fun approveAdmin(adminId: Long, request: AdminApprovalRequest, approvedBy: Long): AdminApprovalResponse {
        logger.info("Admin approval attempt: adminId=$adminId, approved=${request.approved}, approvedBy=$approvedBy")

        request.validate()

        // Admin 정보 조회
        val admin = getAdminForApproval(adminId)
        if (admin.status != "PENDING") {
            throw BusinessException("승인 대기 상태가 아닌 관리자입니다", "INVALID_STATUS")
        }

        // 승인/거부 처리 (임시 하드코딩 - 실제 엔티티 구현 후 교체)
        updateAdminStatus(
            adminId = adminId,
            approved = request.approved,
            approvedBy = approvedBy,
            rejectionReason = request.rejectionReason
        )

        val message = if (request.approved) {
            "관리자가 승인되었습니다"
        } else {
            "관리자가 거부되었습니다: ${request.rejectionReason}"
        }

        logger.info("Admin approval completed: adminId=$adminId, approved=${request.approved}")

        return AdminApprovalResponse(
            adminId = adminId,
            name = admin.name,
            email = admin.email,
            approved = request.approved,
            processedAt = LocalDateTime.now(),
            processedBy = approvedBy,
            rejectionReason = request.rejectionReason,
            message = message
        )
    }

    // 임시 메서드들 - 실제 Repository 구현 후 교체 예정
    private fun isEmailExists(email: String): Boolean {
        // TODO: AdminRepository에서 이메일 존재 여부 확인
        return email == "existing@example.com" // 임시 하드코딩
    }

    private fun validateInstitutionExists(institutionId: Long) {
        // TODO: InstitutionRepository에서 기관 존재 여부 확인
        if (institutionId != 1L) { // 임시 하드코딩
            throw BusinessException("존재하지 않는 기관입니다", "INSTITUTION_NOT_FOUND")
        }
    }

    private fun createInstitution(institutionInfo: InstitutionRegisterRequest): Long {
        // TODO: 새 기관 생성 로직
        logger.info("Creating new institution: ${institutionInfo.name}")
        return 2L // 임시 하드코딩
    }

    private fun createAdmin(
        name: String,
        email: String,
        encodedPassword: String,
        phone: String?,
        role: String,
        institutionId: Long
    ): Long {
        // TODO: Admin 엔티티 생성 및 저장
        logger.info("Creating admin: $name, $email")
        return System.currentTimeMillis() // 임시 ID 생성
    }

    private fun getInstitutionName(institutionId: Long): String {
        // TODO: InstitutionRepository에서 기관명 조회
        return when (institutionId) {
            1L -> "기존 복지관"
            2L -> "새 복지관"
            else -> "알 수 없는 기관"
        }
    }

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

    private fun getAdminForApproval(adminId: Long): AdminApprovalInfo {
        // TODO: AdminRepository에서 승인용 관리자 정보 조회
        return AdminApprovalInfo(
            id = adminId,
            name = "대기중 관리자",
            email = "pending@example.com",
            status = "PENDING"
        )
    }

    private fun updateAdminStatus(
        adminId: Long,
        approved: Boolean,
        approvedBy: Long,
        rejectionReason: String?
    ) {
        // TODO: Admin 상태 업데이트
        logger.info("Updating admin status: adminId=$adminId, approved=$approved")
    }

    // 임시 데이터 클래스들
    private data class AdminInfo(
        val id: Long,
        val institutionId: Long,
        val institutionName: String,
        val status: String
    )

    private data class AdminApprovalInfo(
        val id: Long,
        val name: String,
        val email: String,
        val status: String
    )
}