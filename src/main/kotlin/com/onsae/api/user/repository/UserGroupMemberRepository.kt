package com.onsae.api.user.repository

import com.onsae.api.user.entity.UserGroupMember
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface UserGroupMemberRepository : JpaRepository<UserGroupMember, Long> {
    fun findByGroupId(groupId: Long): List<UserGroupMember>
    fun findByUserId(userId: Long): List<UserGroupMember>
    fun findByGroupIdAndIsActive(groupId: Long, isActive: Boolean): List<UserGroupMember>
    fun existsByGroupIdAndUserId(groupId: Long, userId: Long): Boolean
    fun findByGroupIdAndUserId(groupId: Long, userId: Long): UserGroupMember?

    @Query("SELECT m FROM UserGroupMember m WHERE m.group.institution.id = :institutionId")
    fun findByInstitutionId(institutionId: Long): List<UserGroupMember>

    @Query("SELECT m FROM UserGroupMember m JOIN FETCH m.user JOIN FETCH m.group WHERE m.group.id = :groupId")
    fun findByGroupIdWithUserAndGroup(groupId: Long): List<UserGroupMember>

    fun countByGroupIdAndIsActive(groupId: Long, isActive: Boolean): Int
}