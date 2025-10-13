package com.onsae.api.survey.service

import com.onsae.api.admin.repository.AdminRepository
import com.onsae.api.auth.exception.InvalidCredentialsException
import com.onsae.api.institution.repository.InstitutionRepository
import com.onsae.api.survey.dto.CategoryRequest
import com.onsae.api.survey.dto.CategoryResponse
import com.onsae.api.survey.entity.Category
import com.onsae.api.survey.repository.CategoryRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

@Service
@Transactional
class CategoryService(
    private val categoryRepository: CategoryRepository,
    private val institutionRepository: InstitutionRepository,
    private val adminRepository: AdminRepository
) {

    fun createCategory(request: CategoryRequest, institutionId: Long, adminId: Long): CategoryResponse {
        logger.info("Creating category: ${request.name} for institution: $institutionId")

        if (categoryRepository.existsByInstitutionIdAndName(institutionId, request.name)) {
            throw IllegalArgumentException("이미 존재하는 카테고리 이름입니다")
        }

        val institution = institutionRepository.findById(institutionId)
            .orElseThrow { InvalidCredentialsException("존재하지 않는 기관입니다") }

        val admin = adminRepository.findById(adminId).orElse(null)

        val category = Category().apply {
            this.institution = institution
            this.name = request.name
            this.description = request.description
            this.imagePath = request.imagePath
            this.createdBy = admin
        }

        val savedCategory = categoryRepository.save(category)
        logger.info("Category created successfully: ${savedCategory.id}")

        return toCategoryResponse(savedCategory)
    }

    fun getAllCategories(institutionId: Long): List<CategoryResponse> {
        val categories = categoryRepository.findByInstitutionId(institutionId)
        return categories.map { toCategoryResponse(it) }
    }

    fun getActiveCategories(institutionId: Long): List<CategoryResponse> {
        val categories = categoryRepository.findActiveByInstitutionIdOrderByName(institutionId)
        return categories.map { toCategoryResponse(it) }
    }

    fun getCategoryById(categoryId: Long, institutionId: Long): CategoryResponse {
        val category = categoryRepository.findById(categoryId)
            .orElseThrow { InvalidCredentialsException("존재하지 않는 카테고리입니다") }

        if (category.institution.id != institutionId) {
            throw InvalidCredentialsException("해당 카테고리에 대한 접근 권한이 없습니다")
        }

        return toCategoryResponse(category)
    }

    fun updateCategory(categoryId: Long, request: CategoryRequest, institutionId: Long): CategoryResponse {
        logger.info("Updating category: $categoryId")

        val category = categoryRepository.findById(categoryId)
            .orElseThrow { InvalidCredentialsException("존재하지 않는 카테고리입니다") }

        if (category.institution.id != institutionId) {
            throw InvalidCredentialsException("해당 카테고리에 대한 접근 권한이 없습니다")
        }

        if (category.name != request.name &&
            categoryRepository.existsByInstitutionIdAndName(institutionId, request.name)) {
            throw IllegalArgumentException("이미 존재하는 카테고리 이름입니다")
        }

        category.apply {
            name = request.name
            description = request.description
            imagePath = request.imagePath
        }

        val updatedCategory = categoryRepository.save(category)
        logger.info("Category updated successfully: ${updatedCategory.id}")

        return toCategoryResponse(updatedCategory)
    }

    fun deleteCategory(categoryId: Long, institutionId: Long) {
        logger.info("Deleting category: $categoryId")

        val category = categoryRepository.findById(categoryId)
            .orElseThrow { InvalidCredentialsException("존재하지 않는 카테고리입니다") }

        if (category.institution.id != institutionId) {
            throw InvalidCredentialsException("해당 카테고리에 대한 접근 권한이 없습니다")
        }

        category.isActive = false
        categoryRepository.save(category)
        logger.info("Category deactivated successfully: $categoryId")
    }

    private fun toCategoryResponse(category: Category): CategoryResponse {
        return CategoryResponse(
            id = category.id!!,
            name = category.name,
            description = category.description,
            imagePath = category.imagePath,
            isActive = category.isActive,
            institutionId = category.institution.id!!,
            institutionName = category.institution.name,
            createdById = category.createdBy?.id,
            createdByName = category.createdBy?.name,
            questionCount = category.questions.size,
            createdAt = category.createdAt,
            updatedAt = category.updatedAt
        )
    }
}