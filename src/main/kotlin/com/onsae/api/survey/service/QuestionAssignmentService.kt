package com.onsae.api.survey.service

import com.onsae.api.admin.repository.AdminRepository
import com.onsae.api.auth.exception.InvalidCredentialsException
import com.onsae.api.institution.repository.InstitutionRepository
import com.onsae.api.survey.dto.QuestionAssignmentRequest
import com.onsae.api.survey.dto.QuestionAssignmentResponse
import com.onsae.api.survey.entity.QuestionAssignment
import com.onsae.api.survey.repository.QuestionAssignmentRepository
import com.onsae.api.survey.repository.QuestionRepository
import com.onsae.api.user.repository.UserGroupRepository
import com.onsae.api.user.repository.UserRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

@Service
@Transactional
class QuestionAssignmentService(
    private val questionAssignmentRepository: QuestionAssignmentRepository,
    private val questionRepository: QuestionRepository,
    private val userRepository: UserRepository,
    private val userGroupRepository: UserGroupRepository,
    private val institutionRepository: InstitutionRepository,
    private val adminRepository: AdminRepository
) {

    fun createAssignment(request: QuestionAssignmentRequest, institutionId: Long, adminId: Long): QuestionAssignmentResponse {
        logger.info("Creating question assignment: ${request.questionId} for institution: $institutionId")

        if (request.userId == null && request.groupId == null) {
            throw InvalidCredentialsException("사용자 또는 그룹 중 하나는 반드시 지정해야 합니다")
        }

        if (request.userId != null && request.groupId != null) {
            throw InvalidCredentialsException("사용자와 그룹은 동시에 지정할 수 없습니다")
        }

        val institution = institutionRepository.findById(institutionId)
            .orElseThrow { InvalidCredentialsException("존재하지 않는 기관입니다") }

        val question = questionRepository.findById(request.questionId)
            .orElseThrow { InvalidCredentialsException("존재하지 않는 질문입니다") }

        if (question.institution.id != institutionId) {
            throw InvalidCredentialsException("해당 질문에 대한 접근 권한이 없습니다")
        }

        val user = request.userId?.let { userId ->
            userRepository.findById(userId)
                .orElseThrow { InvalidCredentialsException("존재하지 않는 사용자입니다") }
                .also { user ->
                    if (user.institution.id != institutionId) {
                        throw InvalidCredentialsException("해당 사용자에 대한 접근 권한이 없습니다")
                    }
                    if (questionAssignmentRepository.existsByQuestionIdAndUserId(request.questionId, userId)) {
                        throw InvalidCredentialsException("이미 해당 사용자에게 할당된 질문입니다")
                    }
                }
        }

        val group = request.groupId?.let { groupId ->
            userGroupRepository.findById(groupId)
                .orElseThrow { InvalidCredentialsException("존재하지 않는 사용자 그룹입니다") }
                .also { group ->
                    if (group.institution.id != institutionId) {
                        throw InvalidCredentialsException("해당 사용자 그룹에 대한 접근 권한이 없습니다")
                    }
                    if (questionAssignmentRepository.existsByQuestionIdAndGroupId(request.questionId, groupId)) {
                        throw InvalidCredentialsException("이미 해당 그룹에 할당된 질문입니다")
                    }
                }
        }

        val admin = adminRepository.findById(adminId).orElse(null)

        val assignment = QuestionAssignment().apply {
            this.institution = institution
            this.question = question
            this.user = user
            this.group = group
            this.priority = request.priority
            this.assignedBy = admin
        }

        val savedAssignment = questionAssignmentRepository.save(assignment)
        logger.info("Question assignment created successfully: ${savedAssignment.id}")

        return toQuestionAssignmentResponse(savedAssignment)
    }

    fun getAllAssignments(institutionId: Long): List<QuestionAssignmentResponse> {
        val assignments = questionAssignmentRepository.findByInstitutionIdWithDetails(institutionId)
        return assignments.map { toQuestionAssignmentResponse(it) }
    }

    fun getAssignmentsByUser(userId: Long, institutionId: Long): List<QuestionAssignmentResponse> {
        val user = userRepository.findById(userId)
            .orElseThrow { InvalidCredentialsException("존재하지 않는 사용자입니다") }

        if (user.institution.id != institutionId) {
            throw InvalidCredentialsException("해당 사용자에 대한 접근 권한이 없습니다")
        }

        val assignments = questionAssignmentRepository.findByUserIdWithDetails(userId)
        return assignments.map { toQuestionAssignmentResponse(it) }
    }

    fun getAssignmentsByGroup(groupId: Long, institutionId: Long): List<QuestionAssignmentResponse> {
        val group = userGroupRepository.findById(groupId)
            .orElseThrow { InvalidCredentialsException("존재하지 않는 사용자 그룹입니다") }

        if (group.institution.id != institutionId) {
            throw InvalidCredentialsException("해당 사용자 그룹에 대한 접근 권한이 없습니다")
        }

        val assignments = questionAssignmentRepository.findByGroupIdWithDetails(groupId)
        return assignments.map { toQuestionAssignmentResponse(it) }
    }

    fun getAssignmentById(assignmentId: Long, institutionId: Long): QuestionAssignmentResponse {
        val assignment = questionAssignmentRepository.findById(assignmentId)
            .orElseThrow { InvalidCredentialsException("존재하지 않는 할당입니다") }

        if (assignment.institution.id != institutionId) {
            throw InvalidCredentialsException("해당 할당에 대한 접근 권한이 없습니다")
        }

        return toQuestionAssignmentResponse(assignment)
    }

    fun updateAssignment(assignmentId: Long, request: QuestionAssignmentRequest, institutionId: Long): QuestionAssignmentResponse {
        logger.info("Updating question assignment: $assignmentId")

        val assignment = questionAssignmentRepository.findById(assignmentId)
            .orElseThrow { InvalidCredentialsException("존재하지 않는 할당입니다") }

        if (assignment.institution.id != institutionId) {
            throw InvalidCredentialsException("해당 할당에 대한 접근 권한이 없습니다")
        }

        assignment.priority = request.priority

        val updatedAssignment = questionAssignmentRepository.save(assignment)
        logger.info("Question assignment updated successfully: ${updatedAssignment.id}")

        return toQuestionAssignmentResponse(updatedAssignment)
    }

    fun deleteAssignment(assignmentId: Long, institutionId: Long) {
        logger.info("Deleting question assignment: $assignmentId")

        val assignment = questionAssignmentRepository.findById(assignmentId)
            .orElseThrow { InvalidCredentialsException("존재하지 않는 할당입니다") }

        if (assignment.institution.id != institutionId) {
            throw InvalidCredentialsException("해당 할당에 대한 접근 권한이 없습니다")
        }

        questionAssignmentRepository.delete(assignment)
        logger.info("Question assignment deleted successfully: $assignmentId")
    }

    fun getAssignmentStatistics(institutionId: Long): Map<String, Any> {
        val totalAssignments = questionAssignmentRepository.countByInstitutionId(institutionId)
        val userAssignments = questionAssignmentRepository.findByInstitutionId(institutionId)
            .count { it.user != null }
        val groupAssignments = questionAssignmentRepository.findByInstitutionId(institutionId)
            .count { it.group != null }

        return mapOf(
            "totalAssignments" to totalAssignments,
            "userAssignments" to userAssignments,
            "groupAssignments" to groupAssignments
        )
    }

    private fun toQuestionAssignmentResponse(assignment: QuestionAssignment): QuestionAssignmentResponse {
        return QuestionAssignmentResponse(
            id = assignment.id!!,
            questionId = assignment.question?.id ?: 0L,
            questionTitle = assignment.question?.title ?: "",
            questionContent = assignment.question?.content ?: "",
            userId = assignment.user?.id,
            userName = assignment.user?.name,
            groupId = assignment.group?.id,
            groupName = assignment.group?.name,
            priority = assignment.priority,
            assignedById = assignment.assignedBy?.id,
            assignedByName = assignment.assignedBy?.name,
            assignedAt = assignment.assignedAt,
            responseCount = assignment.responses.size
        )
    }
}