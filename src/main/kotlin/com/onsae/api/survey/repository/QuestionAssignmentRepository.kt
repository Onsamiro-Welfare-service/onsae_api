package com.onsae.api.survey.repository

import com.onsae.api.survey.entity.QuestionAssignment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface QuestionAssignmentRepository : JpaRepository<QuestionAssignment, Long> {
    fun findByInstitutionId(institutionId: Long): List<QuestionAssignment>
    fun findByQuestionId(questionId: Long): List<QuestionAssignment>
    fun findByUserId(userId: Long): List<QuestionAssignment>
    fun findByGroupId(groupId: Long): List<QuestionAssignment>
    fun findByInstitutionIdAndUserId(institutionId: Long, userId: Long): List<QuestionAssignment>
    fun findByInstitutionIdAndGroupId(institutionId: Long, groupId: Long): List<QuestionAssignment>

    @Query("SELECT qa FROM QuestionAssignment qa LEFT JOIN FETCH qa.question LEFT JOIN FETCH qa.user LEFT JOIN FETCH qa.group WHERE qa.institution.id = :institutionId")
    fun findByInstitutionIdWithDetails(institutionId: Long): List<QuestionAssignment>

    @Query("SELECT qa FROM QuestionAssignment qa LEFT JOIN FETCH qa.question LEFT JOIN FETCH qa.user LEFT JOIN FETCH qa.group WHERE qa.user.id = :userId")
    fun findByUserIdWithDetails(userId: Long): List<QuestionAssignment>

    @Query("SELECT qa FROM QuestionAssignment qa LEFT JOIN FETCH qa.question LEFT JOIN FETCH qa.user LEFT JOIN FETCH qa.group WHERE qa.group.id = :groupId")
    fun findByGroupIdWithDetails(groupId: Long): List<QuestionAssignment>

    fun existsByQuestionIdAndUserId(questionId: Long, userId: Long): Boolean
    fun existsByQuestionIdAndGroupId(questionId: Long, groupId: Long): Boolean

    fun countByInstitutionId(institutionId: Long): Long
    fun countByQuestionId(questionId: Long): Long
}