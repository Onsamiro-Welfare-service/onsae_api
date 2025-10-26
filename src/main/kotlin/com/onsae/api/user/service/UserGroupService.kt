package com.onsae.api.user.service

import com.onsae.api.admin.repository.AdminRepository
import com.onsae.api.auth.exception.InvalidCredentialsException
import com.onsae.api.institution.repository.InstitutionRepository
import com.onsae.api.user.dto.*
import com.onsae.api.user.entity.UserGroup
import com.onsae.api.user.entity.UserGroupMember
import com.onsae.api.user.repository.UserGroupMemberRepository
import com.onsae.api.user.repository.UserGroupRepository
import com.onsae.api.user.repository.UserRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

@Service
@Transactional
class UserGroupService(
    private val userGroupRepository: UserGroupRepository,
    private val userGroupMemberRepository: UserGroupMemberRepository,
    private val userRepository: UserRepository,
    private val institutionRepository: InstitutionRepository,
    private val adminRepository: AdminRepository
) {

    fun createUserGroup(request: UserGroupRequest, institutionId: Long, adminId: Long): UserGroupResponse {
        logger.info("Creating user group: ${request.name} for institution: $institutionId")

        if (userGroupRepository.existsByInstitutionIdAndName(institutionId, request.name)) {
            throw IllegalArgumentException("이미 존재하는 그룹 이름입니다")
        }

        val institution = institutionRepository.findById(institutionId)
            .orElseThrow { InvalidCredentialsException("존재하지 않는 기관입니다") }

        val admin = adminRepository.findById(adminId).orElse(null)

        val userGroup = UserGroup().apply {
            this.institution = institution
            this.name = request.name
            this.description = request.description
            this.createdBy = admin
        }

        val savedGroup = userGroupRepository.save(userGroup)
        logger.info("User group created successfully: ${savedGroup.id}")

        return toUserGroupResponse(savedGroup)
    }

    fun getAllUserGroups(institutionId: Long): List<UserGroupResponse> {
        val groups = userGroupRepository.findByInstitutionId(institutionId)
        return groups.map { toUserGroupResponse(it) }
    }

    fun getActiveUserGroups(institutionId: Long): List<UserGroupResponse> {
        val groups = userGroupRepository.findActiveByInstitutionIdOrderByName(institutionId)
        return groups.map { toUserGroupResponse(it) }
    }

    fun getUserGroupById(groupId: Long, institutionId: Long): UserGroupResponse {
        val group = userGroupRepository.findById(groupId)
            .orElseThrow { InvalidCredentialsException("존재하지 않는 그룹입니다") }

        if (group.institution.id != institutionId) {
            throw InvalidCredentialsException("해당 그룹에 대한 접근 권한이 없습니다")
        }

        return toUserGroupResponse(group)
    }

    fun updateUserGroup(groupId: Long, request: UserGroupRequest, institutionId: Long): UserGroupResponse {
        logger.info("Updating user group: $groupId")

        val group = userGroupRepository.findById(groupId)
            .orElseThrow { InvalidCredentialsException("존재하지 않는 그룹입니다") }

        if (group.institution.id != institutionId) {
            throw InvalidCredentialsException("해당 그룹에 대한 접근 권한이 없습니다")
        }

        if (group.name != request.name &&
            userGroupRepository.existsByInstitutionIdAndName(institutionId, request.name)) {
            throw IllegalArgumentException("이미 존재하는 그룹 이름입니다")
        }

        group.apply {
            name = request.name
            description = request.description
        }

        val updatedGroup = userGroupRepository.save(group)
        logger.info("User group updated successfully: ${updatedGroup.id}")

        return toUserGroupResponse(updatedGroup)
    }

    fun deleteUserGroup(groupId: Long, institutionId: Long) {
        logger.info("Deleting user group: $groupId")

        val group = userGroupRepository.findById(groupId)
            .orElseThrow { InvalidCredentialsException("존재하지 않는 그룹입니다") }

        if (group.institution.id != institutionId) {
            throw InvalidCredentialsException("해당 그룹에 대한 접근 권한이 없습니다")
        }

        group.isActive = false
        userGroupRepository.save(group)
        logger.info("User group deactivated successfully: $groupId")
    }

    fun addMembersToGroup(groupId: Long, request: UserGroupMemberRequest, institutionId: Long, adminId: Long): List<UserGroupMemberResponse> {
        logger.info("Adding members to group: $groupId")

        val group = userGroupRepository.findById(groupId)
            .orElseThrow { InvalidCredentialsException("존재하지 않는 그룹입니다") }

        if (group.institution.id != institutionId) {
            throw InvalidCredentialsException("해당 그룹에 대한 접근 권한이 없습니다")
        }

        val admin = adminRepository.findById(adminId).orElse(null)
        val addedMembers = mutableListOf<UserGroupMemberResponse>()

        for (userId in request.userIds) {
            if (!userGroupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
                val user = userRepository.findById(userId)
                    .orElseThrow { InvalidCredentialsException("존재하지 않는 사용자입니다: $userId") }

                if (user.institution.id != institutionId) {
                    throw InvalidCredentialsException("다른 기관의 사용자입니다: $userId")
                }

                val member = UserGroupMember().apply {
                    this.group = group
                    this.user = user
                    this.addedBy = admin
                }

                val savedMember = userGroupMemberRepository.save(member)
                addedMembers.add(toUserGroupMemberResponse(savedMember))
            }
        }

        // 그룹의 멤버 수 업데이트
        group.memberCount = userGroupMemberRepository.countByGroupIdAndIsActive(groupId, true)
        userGroupRepository.save(group)

        logger.info("Added ${addedMembers.size} members to group: $groupId")
        return addedMembers
    }

    fun removeMemberFromGroup(groupId: Long, userId: Long, institutionId: Long) {
        logger.info("Removing user $userId from group: $groupId")

        val group = userGroupRepository.findById(groupId)
            .orElseThrow { InvalidCredentialsException("존재하지 않는 그룹입니다") }

        if (group.institution.id != institutionId) {
            throw InvalidCredentialsException("해당 그룹에 대한 접근 권한이 없습니다")
        }

        val member = userGroupMemberRepository.findByGroupIdAndUserId(groupId, userId)
            ?: throw InvalidCredentialsException("그룹에 속하지 않은 사용자입니다")

        member.isActive = false
        userGroupMemberRepository.save(member)

        // 그룹의 멤버 수 업데이트
        group.memberCount = userGroupMemberRepository.countByGroupIdAndIsActive(groupId, true)
        userGroupRepository.save(group)

        logger.info("Removed user $userId from group: $groupId")
    }

    fun getGroupMembers(groupId: Long, institutionId: Long): List<UserGroupMemberResponse> {
        val group = userGroupRepository.findById(groupId)
            .orElseThrow { InvalidCredentialsException("존재하지 않는 그룹입니다") }

        if (group.institution.id != institutionId) {
            throw InvalidCredentialsException("해당 그룹에 대한 접근 권한이 없습니다")
        }

        val members = userGroupMemberRepository.findByGroupIdWithUserAndGroup(groupId)
        return members.filter { it.isActive }.map { toUserGroupMemberResponse(it) }
    }

    private fun toUserGroupResponse(group: UserGroup): UserGroupResponse {
        return UserGroupResponse(
            id = group.id!!,
            name = group.name,
            description = group.description,
            isActive = group.isActive,
            memberCount = group.memberCount,
            institutionId = group.institution.id!!,
            institutionName = group.institution.name,
            createdById = group.createdBy?.id,
            createdByName = group.createdBy?.name,
            createdAt = group.createdAt,
            updatedAt = group.updatedAt
        )
    }

    private fun toUserGroupMemberResponse(member: UserGroupMember): UserGroupMemberResponse {
        return UserGroupMemberResponse(
            id = member.id!!,
            groupId = member.group.id!!,
            groupName = member.group.name,
            userId = member.user.id!!,
            username = member.user.username,
            userName = member.user.name,
            joinedAt = member.joinedAt,
            isActive = member.isActive,
            addedById = member.addedBy?.id,
            addedByName = member.addedBy?.name
        )
    }
}