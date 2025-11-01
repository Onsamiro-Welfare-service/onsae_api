package com.onsae.api.survey.repository

import com.onsae.api.survey.entity.QuestionResponse
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface QuestionResponseRepository : JpaRepository<QuestionResponse, Long> {
    fun findByAssignmentId(assignmentId: Long): List<QuestionResponse>
    fun findByUserId(userId: Long): List<QuestionResponse>
    fun findByAssignmentIdAndUserId(assignmentId: Long, userId: Long): QuestionResponse?
    fun findByAssignmentIdIn(assignmentIds: List<Long>): List<QuestionResponse>

    fun findByAssignmentIdInAndSubmittedAtBetween(
        assignmentIds: List<Long>,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): List<QuestionResponse>

    @Query("SELECT qr FROM QuestionResponse qr LEFT JOIN FETCH qr.assignment LEFT JOIN FETCH qr.user WHERE qr.assignment.id = :assignmentId")
    fun findByAssignmentIdWithDetails(assignmentId: Long): List<QuestionResponse>

    @Query("SELECT qr FROM QuestionResponse qr LEFT JOIN FETCH qr.assignment LEFT JOIN FETCH qr.user WHERE qr.user.id = :userId")
    fun findByUserIdWithDetails(userId: Long): List<QuestionResponse>

    fun countByAssignmentId(assignmentId: Long): Long
    fun countByUserId(userId: Long): Long

    fun existsByAssignmentIdAndUserId(assignmentId: Long, userId: Long): Boolean

    // Dashboard queries
    @Query("""
        SELECT COUNT(qr) FROM QuestionResponse qr
        WHERE qr.user.institution.id = :institutionId
        AND qr.submittedAt BETWEEN :startDate AND :endDate
    """)
    fun countByInstitutionIdAndSubmittedAtBetween(
        @Param("institutionId") institutionId: Long,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): Int

    @Query("""
        SELECT qr FROM QuestionResponse qr
        LEFT JOIN FETCH qr.user
        LEFT JOIN FETCH qr.question q
        LEFT JOIN FETCH q.category
        WHERE qr.user.institution.id = :institutionId
        AND qr.submittedAt BETWEEN :startDate AND :endDate
        ORDER BY qr.submittedAt DESC
    """)
    fun findByInstitutionIdAndSubmittedAtBetween(
        @Param("institutionId") institutionId: Long,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): List<QuestionResponse>

    @Query("""
        SELECT qr FROM QuestionResponse qr
        LEFT JOIN FETCH qr.user
        LEFT JOIN FETCH qr.question
        WHERE qr.user.institution.id = :institutionId
        ORDER BY qr.submittedAt DESC
    """)
    fun findRecentByInstitutionId(
        @Param("institutionId") institutionId: Long,
        limit: org.springframework.data.domain.Pageable
    ): List<QuestionResponse>

    @Query("""
        SELECT COUNT(qr) FROM QuestionResponse qr
        JOIN qr.assignment a
        JOIN a.group g
        WHERE g.id = :groupId
        AND qr.responseData IS NOT NULL
    """)
    fun countCompletedByGroupId(@Param("groupId") groupId: Long): Int

    // Admin response queries
    @Query("""
        SELECT qr FROM QuestionResponse qr
        LEFT JOIN FETCH qr.assignment a
        LEFT JOIN FETCH qr.user u
        LEFT JOIN FETCH qr.question q
        WHERE u.id = :userId
        AND u.institution.id = :institutionId
        ORDER BY qr.submittedAt DESC
    """)
    fun findByUserIdAndInstitutionIdWithDetails(
        @Param("userId") userId: Long,
        @Param("institutionId") institutionId: Long
    ): List<QuestionResponse>

    @Query("""
        SELECT qr FROM QuestionResponse qr
        LEFT JOIN FETCH qr.assignment a
        LEFT JOIN FETCH qr.user u
        LEFT JOIN FETCH qr.question q
        WHERE a.id = :assignmentId
        AND a.institution.id = :institutionId
        ORDER BY qr.submittedAt DESC
    """)
    fun findByAssignmentIdAndInstitutionIdWithDetails(
        @Param("assignmentId") assignmentId: Long,
        @Param("institutionId") institutionId: Long
    ): List<QuestionResponse>

    @Query("""
        SELECT qr FROM QuestionResponse qr
        LEFT JOIN FETCH qr.assignment a
        LEFT JOIN FETCH qr.user u
        LEFT JOIN FETCH qr.question q
        WHERE u.id = :userId
        AND u.institution.id = :institutionId
        AND qr.submittedAt BETWEEN :startDate AND :endDate
        ORDER BY qr.submittedAt DESC
    """)
    fun findByUserIdAndInstitutionIdAndDateRange(
        @Param("userId") userId: Long,
        @Param("institutionId") institutionId: Long,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): List<QuestionResponse>

    @Query("""
        SELECT qr FROM QuestionResponse qr
        LEFT JOIN FETCH qr.assignment a
        LEFT JOIN FETCH qr.user u
        LEFT JOIN FETCH qr.question q
        WHERE q.id = :questionId
        AND u.id = :userId
        AND u.institution.id = :institutionId
        AND DATE(qr.submittedAt) = DATE(:date)
        ORDER BY qr.submittedAt DESC
    """)
    fun findByQuestionIdAndUserIdAndDateWithDetails(
        @Param("questionId") questionId: Long,
        @Param("userId") userId: Long,
        @Param("institutionId") institutionId: Long,
        @Param("date") date: LocalDateTime
    ): List<QuestionResponse>
}