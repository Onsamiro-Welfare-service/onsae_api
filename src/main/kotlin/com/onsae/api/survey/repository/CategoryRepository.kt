package com.onsae.api.survey.repository

import com.onsae.api.survey.entity.Category
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface CategoryRepository : JpaRepository<Category, Long> {
    fun findByInstitutionId(institutionId: Long): List<Category>
    fun findByInstitutionIdAndIsActive(institutionId: Long, isActive: Boolean): List<Category>
    fun existsByInstitutionIdAndName(institutionId: Long, name: String): Boolean

    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.questions WHERE c.id = :id")
    fun findByIdWithQuestions(id: Long): Category?

    @Query("SELECT c FROM Category c WHERE c.institution.id = :institutionId AND c.isActive = true ORDER BY c.name")
    fun findActiveByInstitutionIdOrderByName(institutionId: Long): List<Category>
}