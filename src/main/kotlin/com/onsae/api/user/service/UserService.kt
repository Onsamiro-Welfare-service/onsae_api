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
import com.onsae.api.user.repository.UserGroupMemberRepository
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
    private val userGroupMemberRepository: UserGroupMemberRepository,
    private val passwordEncoder: org.springframework.security.crypto.password.PasswordEncoder
) {

    fun login(request: UserLoginRequest): LoginResponse {
        logger.info("User login attempt with username: ${request.username}")

        // username으로 사용자 조회
        val user = userRepository.findByUsername(request.username)
            .orElseThrow { InvalidCredentialsException("사용자명 또는 비밀번호가 올바르지 않습니다") }

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.password, user.password)) {
            throw InvalidCredentialsException("사용자명 또는 비밀번호가 올바르지 않습니다")
        }

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

        logger.info("User login successful for username: ${request.username}")

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
            username = user.username,
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
            username = savedUser.username,
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
        val users = userRepository.findByInstitutionIdOrderByCreatedAtDesc(institutionId)

        val userIds = users.mapNotNull { it.id }
        val memberships = if (userIds.isNotEmpty())
            userGroupMemberRepository.findByUserIdInAndIsActive(userIds, true)
        else emptyList()


        val groupsByUserId = memberships.groupBy(
            keySelector = { it.user.id!! },
            valueTransform = { it.group.id!! }
        )
        return users.map { user ->
            UserListResponse(
                id = user.id!!,
                username = user.username,
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
                createdAt = user.createdAt,
                groupIds = groupsByUserId[user.id!!] ?: emptyList()
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
            username = user.username,
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
            username = savedUser.username,
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

    /**
     * 사용자를 소프트 삭제합니다 (관리자용).
     * is_active를 false로 설정하여 비활성화합니다.
     * 같은 기관의 사용자만 삭제 가능합니다.
     * 
     * @param userId 삭제할 사용자 ID
     * @param adminInstitutionId 관리자의 기관 ID
     */
    @Transactional
    fun deleteUser(userId: Long, adminInstitutionId: Long) {
        logger.info("Deleting user: $userId by admin from institution: $adminInstitutionId")

        val user = userRepository.findById(userId)
            .orElseThrow { InvalidCredentialsException("존재하지 않는 사용자입니다") }

        if (user.institution.id != adminInstitutionId) {
            throw InvalidCredentialsException("해당 사용자에 대한 접근 권한이 없습니다")
        }

        // 소프트 삭제: is_active를 false로 설정
        user.isActive = false
        userRepository.save(user)

        logger.info("User deactivated successfully: $userId")
    }
}
