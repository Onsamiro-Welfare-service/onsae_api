package com.onsae.api.user.repository

import com.onsae.api.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByUsername(username: String): Optional<User>
    fun existsByUsername(username: String): Boolean
    fun findByInstitutionIdAndUsername(institutionId: Long, username: String): Optional<User>
    fun existsByInstitutionIdAndUsername(institutionId: Long, username: String): Boolean
    fun findByInstitutionId(institutionId: Long): List<User>
    fun findByInstitutionIdOrderByCreatedAtDesc(institutionId: Long): List<User>
    fun findByIdAndInstitutionId(userId: Long, institutionId: Long): User?

    // Dashboard queries
    fun countByInstitutionId(institutionId: Long): Long
    fun countByInstitutionIdAndIsActiveTrue(institutionId: Long): Long

    @Query("SELECT COUNT(u) FROM User u WHERE u.institution.id = :institutionId AND u.createdAt < :beforeDate")
    fun countByInstitutionIdAndCreatedAtBefore(institutionId: Long, beforeDate: LocalDateTime): Int

    @Query("SELECT COUNT(u) FROM User u WHERE u.institution.id = :institutionId AND u.isActive = true AND (u.lastLogin IS NULL OR u.lastLogin < :beforeDate)")
    fun countByInstitutionIdAndIsActiveTrueAndLastLoginBefore(institutionId: Long, beforeDate: LocalDateTime): Int

    @Query("SELECT u.institution.id as institutionId, COUNT(u) as count FROM User u WHERE u.institution.id IN :institutionIds GROUP BY u.institution.id")
    fun countByInstitutionIds(institutionIds: List<Long>): List<UserCountProjection>

    interface UserCountProjection {
        fun getInstitutionId(): Long
        fun getCount(): Long
    }
}
