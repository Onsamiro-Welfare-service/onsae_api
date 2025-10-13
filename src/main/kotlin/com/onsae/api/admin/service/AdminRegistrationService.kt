package com.onsae.api.admin.service

import com.onsae.api.admin.dto.*
import com.onsae.api.admin.entity.Admin
import com.onsae.api.admin.entity.AdminRole
import com.onsae.api.admin.repository.AdminRepository
import com.onsae.api.auth.exception.InvalidCredentialsException
import com.onsae.api.common.entity.AccountStatus
import com.onsae.api.common.exception.BusinessException
import com.onsae.api.common.exception.DuplicateException
import com.onsae.api.institution.repository.InstitutionRepository
import mu.KotlinLogging
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

@Service
@Transactional
class AdminRegistrationService(
    private val passwordEncoder: PasswordEncoder,
    private val adminRepository: AdminRepository,
    private val institutionRepository: InstitutionRepository
) {

    fun registerAdmin(request: AdminRegisterRequest): AdminRegisterResponse {
        logger.info("Admin registration attempt for email: ${request.email}")

        request.validate()

        // 이메일 중복 검사
        if (isEmailExists(request.email)) {
            throw DuplicateException("이미 등록된 이메일입니다: ${request.email}")
        }

        // 기관 존재 확인
        validateInstitutionExists(request.institutionId)

        // 비밀번호 암호화
        val encodedPassword = passwordEncoder.encode(request.password)

        // Admin 등록
        val adminId = createAdmin(
            name = request.name,
            email = request.email,
            encodedPassword = encodedPassword,
            phone = request.phone,
            role = request.role.name,
            institutionId = request.institutionId
        )

        val institutionName = getInstitutionName(request.institutionId)

        logger.info("Admin registered successfully: adminId=$adminId, email=${request.email}")

        return AdminRegisterResponse(
            adminId = adminId,
            name = request.name,
            email = request.email,
            role = request.role.name,
            institutionId = request.institutionId,
            institutionName = institutionName,
            status = "PENDING",
            createdAt = LocalDateTime.now()
        )
    }

    fun getPendingAdmins(): List<PendingAdminInfo> {
        logger.info("Fetching pending admins")

        return adminRepository.findByStatus(AccountStatus.PENDING).map { admin ->
            PendingAdminInfo(
                id = admin.id!!,
                name = admin.name,
                email = admin.email,
                phone = admin.phone,
                role = admin.role.name,
                institutionId = admin.institution.id!!,
                institutionName = admin.institution.name,
                createdAt = admin.createdAt!!
            )
        }
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

    private fun isEmailExists(email: String): Boolean {
        return adminRepository.existsByEmail(email)
    }

    private fun validateInstitutionExists(institutionId: Long) {
        if (!institutionRepository.existsById(institutionId)) {
            throw BusinessException("존재하지 않는 기관입니다", "INSTITUTION_NOT_FOUND")
        }
    }


    private fun createAdmin(
        name: String,
        email: String,
        encodedPassword: String,
        phone: String?,
        role: String,
        institutionId: Long
    ): Long {
        logger.info("Creating admin: $name, $email")

        val institution = institutionRepository.findById(institutionId)
            .orElseThrow { BusinessException("존재하지 않는 기관입니다", "INSTITUTION_NOT_FOUND") }

        val admin = Admin().apply {
            this.institution = institution
            this.email = email
            this.password = encodedPassword
            this.name = name
            this.role = AdminRole.valueOf(role)
            this.phone = phone
            this.status = AccountStatus.PENDING
        }

        return adminRepository.save(admin).id!!
    }

    private fun getInstitutionName(institutionId: Long): String {
        return institutionRepository.findById(institutionId)
            .map { it.name }
            .orElse("알 수 없는 기관")
    }

    private fun getAdminForApproval(adminId: Long): AdminApprovalInfo {
        val admin = adminRepository.findById(adminId)
            .orElseThrow { BusinessException("존재하지 않는 관리자입니다", "ADMIN_NOT_FOUND") }

        return AdminApprovalInfo(
            id = admin.id!!,
            name = admin.name,
            email = admin.email,
            status = admin.status.name
        )
    }

    private fun updateAdminStatus(
        adminId: Long,
        approved: Boolean,
        approvedBy: Long,
        rejectionReason: String?
    ) {
        val admin = adminRepository.findById(adminId)
            .orElseThrow { BusinessException("존재하지 않는 관리자입니다", "ADMIN_NOT_FOUND") }

        val approver = adminRepository.findById(approvedBy)
            .orElseThrow { BusinessException("승인자 정보를 찾을 수 없습니다", "APPROVER_NOT_FOUND") }

        admin.status = if (approved) AccountStatus.APPROVED else AccountStatus.REJECTED
        admin.approvedBy = approver
        admin.approvedAt = LocalDateTime.now()
        admin.rejectionReason = rejectionReason

        adminRepository.save(admin)
        logger.info("Admin status updated: adminId=$adminId, approved=$approved")
    }

    private data class AdminApprovalInfo(
        val id: Long,
        val name: String,
        val email: String,
        val status: String
    )
}