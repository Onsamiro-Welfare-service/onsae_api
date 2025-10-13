package com.onsae.api.file.repository

import com.onsae.api.file.entity.Upload
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface UploadRepository : JpaRepository<Upload, Long> {
    fun findByUserId(userId: Long): List<Upload>

    @Query("SELECT u FROM Upload u WHERE u.user.institution.id = :institutionId")
    fun findByInstitutionId(@Param("institutionId") institutionId: Long): List<Upload>

    // Dashboard queries
    @Query("""
        SELECT COUNT(u) FROM Upload u
        WHERE u.user.institution.id = :institutionId
        AND u.adminResponseDate IS NULL
    """)
    fun countByInstitutionIdAndProcessedAtIsNull(@Param("institutionId") institutionId: Long): Int

    @Query("""
        SELECT COUNT(u) FROM Upload u
        WHERE u.user.institution.id = :institutionId
        AND u.adminResponseDate IS NULL
        AND u.createdAt < :beforeDate
    """)
    fun countByInstitutionIdAndProcessedAtIsNullAndCreatedAtBefore(
        @Param("institutionId") institutionId: Long,
        @Param("beforeDate") beforeDate: LocalDateTime
    ): Int

    @Query("""
        SELECT u FROM Upload u
        LEFT JOIN FETCH u.user
        LEFT JOIN FETCH u.files
        WHERE u.user.institution.id = :institutionId
        ORDER BY u.createdAt DESC
    """)
    fun findRecentByInstitutionId(
        @Param("institutionId") institutionId: Long,
        limit: org.springframework.data.domain.Pageable
    ): List<Upload>
}
