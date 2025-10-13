package com.onsae.api.user.service

import com.onsae.api.auth.dto.LoginResponse
import com.onsae.api.auth.dto.UserInfo
import com.onsae.api.auth.exception.InvalidCredentialsException
import com.onsae.api.auth.security.JwtTokenProvider
import com.onsae.api.institution.repository.InstitutionRepository
import com.onsae.api.user.dto.UserLoginRequest
import com.onsae.api.user.dto.UserProfileResponse
import com.onsae.api.user.dto.UserUpdateRequest
import com.onsae.api.user.dto.UserListResponse
import com.onsae.api.user.repository.UserRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

@Service
@Transactional
class UserService(
    private val jwtTokenProvider: JwtTokenProvider,
    private val userRepository: UserRepository,
    private val institutionRepository: InstitutionRepository,
    private val temporaryLoginCodeService: TemporaryLoginCodeService
) {

    fun login(request: UserLoginRequest): LoginResponse {
        logger.info("User login attempt with code: ${request.loginCode}")

        // 임시 로그인 코드 검증 및 사용자 ID 조회
        val userId = temporaryLoginCodeService.validateAndConsumeCode(request.loginCode)
            ?: throw InvalidCredentialsException("올바르지 않거나 만료된 로그인 코드입니다")

        val user = userRepository.findById(userId)
            .orElseThrow { InvalidCredentialsException("존재하지 않는 사용자입니다") }

        if (!user.isActive) {
            throw InvalidCredentialsException("비활성화된 계정입니다")
        }

        val institution = user.institution
        if (!institution.isActive) {
            throw InvalidCredentialsException("비활성화된 기관입니다")
        }

        // 로그인 시간 업데이트
        user.lastLogin = LocalDateTime.now()
        userRepository.save(user)

        val authorities = listOf("ROLE_USER")
        val accessToken = jwtTokenProvider.generateAccessToken(
            userId = user.id!!,
            userType = "USER",
            institutionId = institution.id!!,
            authorities = authorities
        )
        val refreshToken = jwtTokenProvider.generateRefreshToken(user.id!!)
        val expiresAt = LocalDateTime.now().plusHours(24)

        logger.info("User login successful for code: ${request.loginCode}")

        return LoginResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresAt = expiresAt,
            user = UserInfo(
                id = user.id!!,
                userType = "USER",
                name = user.name,
                email = null,
                institutionId = institution.id!!,
                institutionName = institution.name,
                authorities = authorities
            )
        )
    }

    fun getUserProfile(userId: Long): UserProfileResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { InvalidCredentialsException("존재하지 않는 사용자입니다") }

        return UserProfileResponse(
            id = user.id!!,
            usercode = user.usercode,
            name = user.name,
            phone = user.phone,
            address = user.address,
            birthDate = user.birthDate,
            severity = user.severity,
            guardianName = user.guardianName,
            guardianRelationship = user.guardianRelationship,
            guardianPhone = user.guardianPhone,
            guardianEmail = user.guardianEmail,
            guardianAddress = user.guardianAddress,
            emergencyContacts = user.emergencyContacts,
            careNotes = user.careNotes,
            isActive = user.isActive,
            lastLogin = user.lastLogin,
            institutionId = user.institution.id!!,
            institutionName = user.institution.name,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt
        )
    }

    fun updateUserProfile(userId: Long, request: UserUpdateRequest): UserProfileResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { InvalidCredentialsException("존재하지 않는 사용자입니다") }

        user.name = request.name
        user.phone = request.phone
        user.address = request.address
        user.birthDate = request.birthDate
        user.severity = request.severity
        user.guardianName = request.guardianName
        user.guardianRelationship = request.guardianRelationship
        user.guardianPhone = request.guardianPhone
        user.guardianEmail = request.guardianEmail
        user.guardianAddress = request.guardianAddress
        user.emergencyContacts = request.emergencyContacts
        user.careNotes = request.careNotes

        val savedUser = userRepository.save(user)

        return UserProfileResponse(
            id = savedUser.id!!,
            usercode = savedUser.usercode,
            name = savedUser.name,
            phone = savedUser.phone,
            address = savedUser.address,
            birthDate = savedUser.birthDate,
            severity = savedUser.severity,
            guardianName = savedUser.guardianName,
            guardianRelationship = savedUser.guardianRelationship,
            guardianPhone = savedUser.guardianPhone,
            guardianEmail = savedUser.guardianEmail,
            guardianAddress = savedUser.guardianAddress,
            emergencyContacts = savedUser.emergencyContacts,
            careNotes = savedUser.careNotes,
            isActive = savedUser.isActive,
            lastLogin = savedUser.lastLogin,
            institutionId = savedUser.institution.id!!,
            institutionName = savedUser.institution.name,
            createdAt = savedUser.createdAt,
            updatedAt = savedUser.updatedAt
        )
    }

    fun getAllUsers(institutionId: Long): List<UserListResponse> {
        val users = userRepository.findByInstitutionId(institutionId)
        return users.map { user ->
            UserListResponse(
                id = user.id!!,
                usercode = user.usercode,
                name = user.name,
                phone = user.phone,
                birthDate = user.birthDate,
                severity = user.severity,
                guardianName = user.guardianName,
                guardianPhone = user.guardianPhone,
                isActive = user.isActive,
                lastLogin = user.lastLogin,
                institutionId = user.institution.id!!,
                institutionName = user.institution.name,
                createdAt = user.createdAt
            )
        }
    }

    fun getUserProfileByAdmin(userId: Long, adminInstitutionId: Long): UserProfileResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { InvalidCredentialsException("존재하지 않는 사용자입니다") }

        if (user.institution.id != adminInstitutionId) {
            throw InvalidCredentialsException("해당 사용자에 대한 접근 권한이 없습니다")
        }

        return UserProfileResponse(
            id = user.id!!,
            usercode = user.usercode,
            name = user.name,
            phone = user.phone,
            address = user.address,
            birthDate = user.birthDate,
            severity = user.severity,
            guardianName = user.guardianName,
            guardianRelationship = user.guardianRelationship,
            guardianPhone = user.guardianPhone,
            guardianEmail = user.guardianEmail,
            guardianAddress = user.guardianAddress,
            emergencyContacts = user.emergencyContacts,
            careNotes = user.careNotes,
            isActive = user.isActive,
            lastLogin = user.lastLogin,
            institutionId = user.institution.id!!,
            institutionName = user.institution.name,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt
        )
    }

    fun updateUserProfileByAdmin(userId: Long, request: UserUpdateRequest, adminInstitutionId: Long): UserProfileResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { InvalidCredentialsException("존재하지 않는 사용자입니다") }

        if (user.institution.id != adminInstitutionId) {
            throw InvalidCredentialsException("해당 사용자에 대한 접근 권한이 없습니다")
        }

        user.name = request.name
        user.phone = request.phone
        user.address = request.address
        user.birthDate = request.birthDate
        user.severity = request.severity
        user.guardianName = request.guardianName
        user.guardianRelationship = request.guardianRelationship
        user.guardianPhone = request.guardianPhone
        user.guardianEmail = request.guardianEmail
        user.guardianAddress = request.guardianAddress
        user.emergencyContacts = request.emergencyContacts
        user.careNotes = request.careNotes

        val savedUser = userRepository.save(user)

        return UserProfileResponse(
            id = savedUser.id!!,
            usercode = savedUser.usercode,
            name = savedUser.name,
            phone = savedUser.phone,
            address = savedUser.address,
            birthDate = savedUser.birthDate,
            severity = savedUser.severity,
            guardianName = savedUser.guardianName,
            guardianRelationship = savedUser.guardianRelationship,
            guardianPhone = savedUser.guardianPhone,
            guardianEmail = savedUser.guardianEmail,
            guardianAddress = savedUser.guardianAddress,
            emergencyContacts = savedUser.emergencyContacts,
            careNotes = savedUser.careNotes,
            isActive = savedUser.isActive,
            lastLogin = savedUser.lastLogin,
            institutionId = savedUser.institution.id!!,
            institutionName = savedUser.institution.name,
            createdAt = savedUser.createdAt,
            updatedAt = savedUser.updatedAt
        )
    }
}