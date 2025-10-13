package com.onsae.api.user.repository

import com.onsae.api.user.entity.UserGroup
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface UserGroupRepository : JpaRepository<UserGroup, Long> {
    fun findByInstitutionId(institutionId: Long): List<UserGroup>
    fun findByInstitutionIdAndIsActive(institutionId: Long, isActive: Boolean): List<UserGroup>
    fun existsByInstitutionIdAndName(institutionId: Long, name: String): Boolean

    @Query("SELECT g FROM UserGroup g LEFT JOIN FETCH g.members WHERE g.id = :id")
    fun findByIdWithMembers(id: Long): UserGroup?

    @Query("SELECT g FROM UserGroup g WHERE g.institution.id = :institutionId AND g.isActive = true ORDER BY g.name")
    fun findActiveByInstitutionIdOrderByName(institutionId: Long): List<UserGroup>
}