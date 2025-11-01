package com.onsae.api.survey.service

import com.onsae.api.auth.exception.InvalidCredentialsException
import com.onsae.api.survey.dto.QuestionResponseRequest
import com.onsae.api.survey.dto.UserQuestionResponse
import com.onsae.api.survey.entity.QuestionResponse
import com.onsae.api.survey.repository.QuestionAssignmentRepository
import com.onsae.api.survey.repository.QuestionResponseRepository
import com.onsae.api.user.repository.UserGroupMemberRepository
import com.onsae.api.user.repository.UserRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

@Service
@Transactional
class UserQuestionService(
    private val userRepository: UserRepository,
    private val userGroupMemberRepository: UserGroupMemberRepository,
    private val questionAssignmentRepository: QuestionAssignmentRepository,
    private val questionResponseRepository: QuestionResponseRepository
) {

    fun getMyQuestions(userId: Long): List<UserQuestionResponse> {
        logger.info("Getting questions for user: $userId")

        val user = userRepository.findById(userId)
            .orElseThrow { InvalidCredentialsException("존재하지 않는 사용자입니다") }

        // 1. 사용자가 속한 그룹 ID들 조회
        val userGroupIds = userGroupMemberRepository.findByUserId(userId)
            .map { it.group.id!! }

        // 2. 개별 할당 + 그룹 할당 질문들 조회
        val directAssignments = questionAssignmentRepository.findByUserIdWithDetails(userId)
        val groupAssignments = userGroupIds.flatMap { groupId ->
            questionAssignmentRepository.findByGroupIdWithDetails(groupId)
        }

        val allAssignments = (directAssignments + groupAssignments).distinctBy { it.question?.id!! }

        // 3. 오늘 날짜 기준으로 응답 완료 여부 확인
        val today = LocalDate.now()
        val startOfDay = today.atStartOfDay()
        val endOfDay = today.plusDays(1).atStartOfDay()

        val assignmentIds = allAssignments.map { it.id!! }
        val completedResponses = questionResponseRepository.findByAssignmentIdInAndSubmittedAtBetween(
            assignmentIds, startOfDay, endOfDay
        ).associateBy { it.assignment?.id!! }

        // 4. UserQuestionResponse로 변환
        return allAssignments
            .filter { it.question?.isActive == true }
            .map { assignment ->
                val response = completedResponses[assignment.id]
                val isGroupAssignment = assignment.group != null

                UserQuestionResponse(
                    assignmentId = assignment.id!!,
                    questionId = assignment.question?.id ?: 0L,
                    title = assignment.question?.title ?: "",
                    content = assignment.question?.content ?: "",
                    questionType = assignment.question?.questionType!!,
                    categoryId = assignment.question?.category?.id,
                    categoryName = assignment.question?.category?.name,
                    options = assignment.question?.options,
                    allowOtherOption = assignment.question?.allowOtherOption ?: false,
                    otherOptionLabel = assignment.question?.otherOptionLabel ?: "기타",
                    otherOptionPlaceholder = assignment.question?.otherOptionPlaceholder,
                    isRequired = assignment.question?.isRequired ?: false,
                    priority = assignment.priority,
                    assignmentSource = if (isGroupAssignment) "GROUP" else "USER",
                    sourceId = if (isGroupAssignment) assignment.group?.id else assignment.user?.id,
                    sourceName = if (isGroupAssignment) assignment.group?.name else assignment.user?.name,
                    isCompleted = response != null,
                    responseId = response?.id,
                    responseAnswer = response?.responseData,
                    responseSubmittedAt = response?.submittedAt,
                    assignedAt = assignment.assignedAt
                )
            }
            .sortedWith(compareBy<UserQuestionResponse> { it.isCompleted }
                .thenBy { it.priority }
                .thenByDescending { it.assignedAt })
    }

    fun submitResponse(request: QuestionResponseRequest, userId: Long): UserQuestionResponse {
        logger.info("Submitting response for assignment: ${request.assignmentId} by user: $userId")

        val user = userRepository.findById(userId)
            .orElseThrow { InvalidCredentialsException("존재하지 않는 사용자입니다") }

        val assignment = questionAssignmentRepository.findById(request.assignmentId)
            .orElseThrow { InvalidCredentialsException("존재하지 않는 할당입니다") }

        // 할당 검증: 개별 할당이거나 사용자가 해당 그룹에 속해 있는지 확인
        val hasAccess = when {
            assignment.user?.id == userId -> true
            assignment.group != null -> {
                userGroupMemberRepository.findByUserId(userId)
                    .any { it.group.id == assignment.group?.id }
            }
            else -> false
        }

        if (!hasAccess) {
            throw InvalidCredentialsException("해당 질문에 대한 접근 권한이 없습니다")
        }

        // 응답 저장
        val questionResponse = QuestionResponse().apply {
            this.assignment = assignment
            this.user = user
            this.question = assignment.question
            this.responseData = request.answer
            this.submittedAt = LocalDateTime.now()
        }

        val savedResponse = questionResponseRepository.save(questionResponse)
        logger.info("Response submitted successfully: ${savedResponse.id}")

        // 업데이트된 질문 정보 반환
        return getMyQuestions(userId).first { it.assignmentId == request.assignmentId }
    }

    fun getMyQuestionById(assignmentId: Long, userId: Long): UserQuestionResponse {
        val myQuestions = getMyQuestions(userId)
        return myQuestions.find { it.assignmentId == assignmentId }
            ?: throw InvalidCredentialsException("해당 질문에 대한 접근 권한이 없습니다")
    }

    fun getMyQuestionStatistics(userId: Long): Map<String, Any> {
        val myQuestions = getMyQuestions(userId)
        val completedCount = myQuestions.count { it.isCompleted }
        val pendingCount = myQuestions.count { !it.isCompleted }

        return mapOf(
            "totalQuestions" to myQuestions.size,
            "completedQuestions" to completedCount,
            "pendingQuestions" to pendingCount,
            "completionRate" to if (myQuestions.isNotEmpty()) (completedCount.toDouble() / myQuestions.size * 100).toInt() else 0
        )
    }
}