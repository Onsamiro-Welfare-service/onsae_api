package com.onsae.api.user.repository

import com.onsae.api.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByUsercode(usercode: String): Optional<User>
    fun existsByUsercode(usercode: String): Boolean
    fun findByInstitutionIdAndUsercode(institutionId: Long, usercode: String): Optional<User>
    fun existsByInstitutionIdAndUsercode(institutionId: Long, usercode: String): Boolean
    fun findByInstitutionId(institutionId: Long): List<User>

    // Dashboard queries
    fun countByInstitutionId(institutionId: Long): Int
    fun countByInstitutionIdAndIsActiveTrue(institutionId: Long): Int

    @Query("SELECT COUNT(u) FROM User u WHERE u.institution.id = :institutionId AND u.createdAt < :beforeDate")
    fun countByInstitutionIdAndCreatedAtBefore(institutionId: Long, beforeDate: LocalDateTime): Int

    @Query("SELECT COUNT(u) FROM User u WHERE u.institution.id = :institutionId AND u.isActive = true AND (u.lastLogin IS NULL OR u.lastLogin < :beforeDate)")
    fun countByInstitutionIdAndIsActiveTrueAndLastLoginBefore(institutionId: Long, beforeDate: LocalDateTime): Int
}