package com.onsae.api.survey.repository

import com.onsae.api.survey.entity.Question
import com.onsae.api.survey.entity.QuestionType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface QuestionRepository : JpaRepository<Question, Long> {
    fun findByInstitutionId(institutionId: Long): List<Question>
    fun findByInstitutionIdAndIsActive(institutionId: Long, isActive: Boolean): List<Question>
    fun findByCategoryId(categoryId: Long): List<Question>
    fun findByCategoryIdAndIsActive(categoryId: Long, isActive: Boolean): List<Question>
    fun findByQuestionType(questionType: QuestionType): List<Question>
    fun findByInstitutionIdAndQuestionType(institutionId: Long, questionType: QuestionType): List<Question>

    @Query("SELECT q FROM Question q LEFT JOIN FETCH q.category WHERE q.id = :id")
    fun findByIdWithCategory(id: Long): Question?

    @Query("SELECT q FROM Question q LEFT JOIN FETCH q.assignments WHERE q.id = :id")
    fun findByIdWithAssignments(id: Long): Question?

    @Query("SELECT q FROM Question q WHERE q.institution.id = :institutionId AND q.isActive = true ORDER BY q.createdAt DESC")
    fun findActiveByInstitutionIdOrderByCreatedAtDesc(institutionId: Long): List<Question>

    @Query("SELECT q FROM Question q WHERE q.category.id = :categoryId AND q.isActive = true ORDER BY q.createdAt DESC")
    fun findActiveByCategoryIdOrderByCreatedAtDesc(categoryId: Long): List<Question>

    @Query("SELECT q FROM Question q WHERE q.institution.id = :institutionId AND (:categoryId IS NULL OR q.category.id = :categoryId) AND q.isActive = true")
    fun findByInstitutionIdAndCategoryIdAndIsActive(institutionId: Long, categoryId: Long?): List<Question>

    fun countByInstitutionId(institutionId: Long): Long
    fun countByCategoryId(categoryId: Long): Long
    fun countByInstitutionIdAndIsActive(institutionId: Long, isActive: Boolean): Long
}