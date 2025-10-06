package com.onsae.api.survey.repository

import com.onsae.api.survey.entity.QuestionResponse
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface QuestionResponseRepository : JpaRepository<QuestionResponse, Long> {
    fun findByAssignmentId(assignmentId: Long): List<QuestionResponse>
    fun findByUserId(userId: Long): List<QuestionResponse>
    fun findByAssignmentIdAndUserId(assignmentId: Long, userId: Long): QuestionResponse?
    fun findByAssignmentIdIn(assignmentIds: List<Long>): List<QuestionResponse>

    @Query("SELECT qr FROM QuestionResponse qr LEFT JOIN FETCH qr.assignment LEFT JOIN FETCH qr.user WHERE qr.assignment.id = :assignmentId")
    fun findByAssignmentIdWithDetails(assignmentId: Long): List<QuestionResponse>

    @Query("SELECT qr FROM QuestionResponse qr LEFT JOIN FETCH qr.assignment LEFT JOIN FETCH qr.user WHERE qr.user.id = :userId")
    fun findByUserIdWithDetails(userId: Long): List<QuestionResponse>

    fun countByAssignmentId(assignmentId: Long): Long
    fun countByUserId(userId: Long): Long

    fun existsByAssignmentIdAndUserId(assignmentId: Long, userId: Long): Boolean
}