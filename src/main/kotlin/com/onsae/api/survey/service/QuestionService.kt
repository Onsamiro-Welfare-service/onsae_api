package com.onsae.api.survey.service

import com.onsae.api.admin.repository.AdminRepository
import com.onsae.api.auth.exception.InvalidCredentialsException
import com.onsae.api.institution.repository.InstitutionRepository
import com.onsae.api.survey.dto.QuestionRequest
import com.onsae.api.survey.dto.QuestionResponse
import com.onsae.api.survey.entity.Question
import com.onsae.api.survey.entity.QuestionType
import com.onsae.api.survey.repository.CategoryRepository
import com.onsae.api.survey.repository.QuestionRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

@Service
@Transactional
class QuestionService(
    private val questionRepository: QuestionRepository,
    private val categoryRepository: CategoryRepository,
    private val institutionRepository: InstitutionRepository,
    private val adminRepository: AdminRepository
) {

    fun createQuestion(request: QuestionRequest, institutionId: Long, adminId: Long): QuestionResponse {
        logger.info("Creating question: ${request.title} for institution: $institutionId")

        val institution = institutionRepository.findById(institutionId)
            .orElseThrow { InvalidCredentialsException("존재하지 않는 기관입니다") }

        val category = request.categoryId?.let { categoryId ->
            categoryRepository.findById(categoryId)
                .orElseThrow { InvalidCredentialsException("존재하지 않는 카테고리입니다") }
                .also { category ->
                    if (category.institution.id != institutionId) {
                        throw InvalidCredentialsException("해당 카테고리에 대한 접근 권한이 없습니다")
                    }
                }
        }

        val admin = adminRepository.findById(adminId).orElse(null)

        val question = Question().apply {
            this.institution = institution
            this.category = category
            this.title = request.title
            this.content = request.content
            this.questionType = request.questionType
            this.options = request.options
            this.allowOtherOption = request.allowOtherOption
            this.otherOptionLabel = request.otherOptionLabel
            this.otherOptionPlaceholder = request.otherOptionPlaceholder
            this.isRequired = request.isRequired
            this.createdBy = admin
        }

        val savedQuestion = questionRepository.save(question)
        logger.info("Question created successfully: ${savedQuestion.id}")

        return toQuestionResponse(savedQuestion)
    }

    fun getAllQuestions(institutionId: Long, categoryId: Long?): List<QuestionResponse> {
        val questions = if (categoryId != null) {
            questionRepository.findByInstitutionIdAndCategoryIdAndIsActive(institutionId, categoryId)
        } else {
            questionRepository.findByInstitutionId(institutionId)
        }
        return questions.map { toQuestionResponse(it) }
    }

    fun getActiveQuestions(institutionId: Long, categoryId: Long?): List<QuestionResponse> {
        val questions = if (categoryId != null) {
            questionRepository.findActiveByCategoryIdOrderByCreatedAtDesc(categoryId)
        } else {
            questionRepository.findActiveByInstitutionIdOrderByCreatedAtDesc(institutionId)
        }
        return questions.map { toQuestionResponse(it) }
    }

    fun getQuestionsByType(institutionId: Long, questionType: QuestionType): List<QuestionResponse> {
        val questions = questionRepository.findByInstitutionIdAndQuestionType(institutionId, questionType)
        return questions.map { toQuestionResponse(it) }
    }

    fun getQuestionById(questionId: Long, institutionId: Long): QuestionResponse {
        val question = questionRepository.findById(questionId)
            .orElseThrow { InvalidCredentialsException("존재하지 않는 질문입니다") }

        if (question.institution.id != institutionId) {
            throw InvalidCredentialsException("해당 질문에 대한 접근 권한이 없습니다")
        }

        return toQuestionResponse(question)
    }

    fun updateQuestion(questionId: Long, request: QuestionRequest, institutionId: Long): QuestionResponse {
        logger.info("Updating question: $questionId")

        val question = questionRepository.findById(questionId)
            .orElseThrow { InvalidCredentialsException("존재하지 않는 질문입니다") }

        if (question.institution.id != institutionId) {
            throw InvalidCredentialsException("해당 질문에 대한 접근 권한이 없습니다")
        }

        val category = request.categoryId?.let { categoryId ->
            categoryRepository.findById(categoryId)
                .orElseThrow { InvalidCredentialsException("존재하지 않는 카테고리입니다") }
                .also { category ->
                    if (category.institution.id != institutionId) {
                        throw InvalidCredentialsException("해당 카테고리에 대한 접근 권한이 없습니다")
                    }
                }
        }

        question.apply {
            this.category = category
            this.title = request.title
            this.content = request.content
            this.questionType = request.questionType
            this.options = request.options
            this.allowOtherOption = request.allowOtherOption
            this.otherOptionLabel = request.otherOptionLabel
            this.otherOptionPlaceholder = request.otherOptionPlaceholder
            this.isRequired = request.isRequired
        }

        val updatedQuestion = questionRepository.save(question)
        logger.info("Question updated successfully: ${updatedQuestion.id}")

        return toQuestionResponse(updatedQuestion)
    }

    fun deleteQuestion(questionId: Long, institutionId: Long) {
        logger.info("Deleting question: $questionId")

        val question = questionRepository.findById(questionId)
            .orElseThrow { InvalidCredentialsException("존재하지 않는 질문입니다") }

        if (question.institution.id != institutionId) {
            throw InvalidCredentialsException("해당 질문에 대한 접근 권한이 없습니다")
        }

        question.isActive = false
        questionRepository.save(question)
        logger.info("Question deactivated successfully: $questionId")
    }

    fun getQuestionStatistics(institutionId: Long): Map<String, Any> {
        val totalQuestions = questionRepository.countByInstitutionId(institutionId)
        val activeQuestions = questionRepository.countByInstitutionIdAndIsActive(institutionId, true)

        return mapOf(
            "totalQuestions" to totalQuestions,
            "activeQuestions" to activeQuestions,
            "inactiveQuestions" to (totalQuestions - activeQuestions)
        )
    }

    private fun toQuestionResponse(question: Question): QuestionResponse {
        return QuestionResponse(
            id = question.id!!,
            title = question.title,
            content = question.content,
            questionType = question.questionType,
            categoryId = question.category?.id,
            categoryName = question.category?.name,
            options = question.options,
            allowOtherOption = question.allowOtherOption,
            otherOptionLabel = question.otherOptionLabel,
            otherOptionPlaceholder = question.otherOptionPlaceholder,
            isRequired = question.isRequired,
            isActive = question.isActive,
            institutionId = question.institution.id!!,
            institutionName = question.institution.name,
            createdById = question.createdBy?.id,
            createdByName = question.createdBy?.name,
            assignmentCount = question.assignments.size,
            responseCount = question.responses.size,
            createdAt = question.createdAt,
            updatedAt = question.updatedAt
        )
    }
}