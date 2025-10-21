package com.onsae.api.institution.service

import com.onsae.api.admin.repository.AdminRepository
import com.onsae.api.common.exception.BusinessException
import com.onsae.api.common.exception.DuplicateException
import com.onsae.api.institution.dto.*
import com.onsae.api.institution.entity.Institution
import com.onsae.api.institution.repository.InstitutionRepository
import com.onsae.api.user.repository.UserRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

@Service
@Transactional(readOnly = true)
class InstitutionService(
    private val institutionRepository: InstitutionRepository,
    private val adminRepository: AdminRepository,
    private val userRepository: UserRepository
) {

    fun getActiveInstitutions(): List<InstitutionListResponse> {
        logger.info("Fetching active institutions")

        return institutionRepository.findByIsActiveTrue().map { institution ->
            val institutionId = institution.id!!
            val adminCount = adminRepository.countByInstitutionId(institutionId)
            val userCount = userRepository.countByInstitutionId(institutionId)

            InstitutionListResponse(
                id = institutionId,
                name = institution.name,
                businessNumber = institution.businessNumber,
                address = institution.address,
                phone = institution.phone,
                directorName = institution.directorName,
                adminCount = adminCount,
                userCount = userCount,
                isActive = institution.isActive,
                createdAt = institution.createdAt!!
            )
        }
    }

    fun getInstitution(id: Long): InstitutionDetailResponse {
        logger.info("Fetching institution: $id")

        val institution = institutionRepository.findById(id)
            .orElseThrow { BusinessException("존재하지 않는 기관입니다", "INSTITUTION_NOT_FOUND") }

        val adminCount = adminRepository.countByInstitutionId(id)
        val userCount = userRepository.countByInstitutionId(id)

        return InstitutionDetailResponse(
            id = institution.id!!,
            name = institution.name,
            businessNumber = institution.businessNumber,
            registrationNumber = institution.registrationNumber,
            address = institution.address,
            phone = institution.phone,
            email = institution.email,
            directorName = institution.directorName,
            website = institution.website,
            contactPerson = institution.contactPerson,
            contactPhone = institution.contactPhone,
            contactEmail = institution.contactEmail,
            isActive = institution.isActive,
            timezone = institution.timezone,
            locale = institution.locale,
            adminCount = adminCount,
            userCount = userCount,
            createdAt = institution.createdAt!!,
            updatedAt = institution.updatedAt!!
        )
    }

    @Transactional
    fun createInstitution(request: InstitutionCreateRequest): InstitutionDetailResponse {
        logger.info("Creating institution: ${request.name}")

        // 중복 검사
        if (institutionRepository.existsByName(request.name)) {
            throw DuplicateException("이미 등록된 기관명입니다: ${request.name}")
        }

        request.businessNumber?.let { businessNumber ->
            if (institutionRepository.existsByBusinessNumber(businessNumber)) {
                throw DuplicateException("이미 등록된 사업자등록번호입니다: $businessNumber")
            }
        }

        val institution = Institution().apply {
            name = request.name
            businessNumber = request.businessNumber
            registrationNumber = request.registrationNumber
            address = request.address
            phone = request.phone
            email = request.email
            directorName = request.directorName
            website = request.website
            contactPerson = request.contactPerson
            contactPhone = request.contactPhone
            contactEmail = request.contactEmail
            isActive = true
        }

        val savedInstitution = institutionRepository.save(institution)
        logger.info("Institution created successfully: ${savedInstitution.id}")

        return getInstitution(savedInstitution.id!!)
    }

    @Transactional
    fun updateInstitution(id: Long, request: InstitutionUpdateRequest): InstitutionDetailResponse {
        logger.info("Updating institution: $id")

        val institution = institutionRepository.findById(id)
            .orElseThrow { BusinessException("존재하지 않는 기관입니다", "INSTITUTION_NOT_FOUND") }

        // 중복 검사 (본인 제외)
        request.name?.let { name ->
            if (name != institution.name && institutionRepository.existsByName(name)) {
                throw DuplicateException("이미 등록된 기관명입니다: $name")
            }
        }

        request.businessNumber?.let { businessNumber ->
            if (businessNumber != institution.businessNumber && institutionRepository.existsByBusinessNumber(businessNumber)) {
                throw DuplicateException("이미 등록된 사업자등록번호입니다: $businessNumber")
            }
        }

        // 필드 업데이트
        request.name?.let { institution.name = it }
        request.businessNumber?.let { institution.businessNumber = it }
        request.registrationNumber?.let { institution.registrationNumber = it }
        request.address?.let { institution.address = it }
        request.phone?.let { institution.phone = it }
        request.email?.let { institution.email = it }
        request.directorName?.let { institution.directorName = it }
        request.website?.let { institution.website = it }
        request.contactPerson?.let { institution.contactPerson = it }
        request.contactPhone?.let { institution.contactPhone = it }
        request.contactEmail?.let { institution.contactEmail = it }
        request.isActive?.let { institution.isActive = it }

        institutionRepository.save(institution)
        logger.info("Institution updated successfully: $id")

        return getInstitution(id)
    }

    @Transactional
    fun deleteInstitution(id: Long) {
        logger.info("Deleting institution: $id")

        val institution = institutionRepository.findById(id)
            .orElseThrow { BusinessException("존재하지 않는 기관입니다", "INSTITUTION_NOT_FOUND") }

        // 연결된 관리자나 사용자가 있는지 확인
        if (institution.admins.isNotEmpty() || institution.users.isNotEmpty()) {
            throw BusinessException("기관에 연결된 관리자나 사용자가 있어 삭제할 수 없습니다", "INSTITUTION_HAS_DEPENDENCIES")
        }

        // 실제 삭제 대신 비활성화
        institution.isActive = false
        institutionRepository.save(institution)

        logger.info("Institution deactivated successfully: $id")
    }
}