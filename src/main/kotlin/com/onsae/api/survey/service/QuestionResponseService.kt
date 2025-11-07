package com.onsae.api.survey.service

import com.onsae.api.survey.dto.AssignmentResponseSummaryDTO
import com.onsae.api.survey.dto.QuestionResponseDetailDTO
import com.onsae.api.survey.dto.UserResponseSummaryDTO
import com.onsae.api.survey.entity.QuestionResponse
import com.onsae.api.survey.exception.QuestionAssignmentNotFoundException
import com.onsae.api.survey.exception.QuestionNotFoundException
import com.onsae.api.survey.repository.QuestionResponseRepository
import com.onsae.api.user.exception.UserNotFoundException
import com.onsae.api.user.repository.UserRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

@Service
@Transactional(readOnly = true)
class QuestionResponseService(
    private val questionResponseRepository: QuestionResponseRepository,
    private val userRepository: UserRepository
) {

    /**
     * 특정 사용자의 모든 응답 조회 (같은 날 중복 답변은 최신 것만 반환)
     */
    fun getUserResponses(userId: Long, institutionId: Long): UserResponseSummaryDTO {
        logger.info { "Fetching responses for user: $userId in institution: $institutionId" }

        // 사용자 확인
        val user = userRepository.findByIdAndInstitutionId(userId, institutionId)
            ?: throw UserNotFoundException("User not found: $userId")

        val responses = questionResponseRepository.findByUserIdAndInstitutionIdWithDetails(userId, institutionId)
        val responsesWithModificationInfo = addModificationInfoWithLatestOnly(responses)

        return UserResponseSummaryDTO(
            userId = user.id!!,
            userName = user.name,
            totalResponses = responsesWithModificationInfo.size,
            latestResponseAt = responsesWithModificationInfo.maxOfOrNull { it.submittedAt },
            responses = responsesWithModificationInfo
        )
    }

    /**
     * 특정 할당에 대한 모든 응답 조회 (같은 날 중복 답변은 최신 것만 반환)
     */
    fun getAssignmentResponses(assignmentId: Long, institutionId: Long): AssignmentResponseSummaryDTO {
        logger.info { "Fetching responses for assignment: $assignmentId in institution: $institutionId" }

        val responses = questionResponseRepository.findByAssignmentIdAndInstitutionIdWithDetails(assignmentId, institutionId)

        if (responses.isEmpty()) {
            throw QuestionAssignmentNotFoundException("Assignment not found or has no responses: $assignmentId")
        }

        val firstResponse = responses.first()
        val question = firstResponse.question ?: throw QuestionNotFoundException("Question not found for assignment: $assignmentId")
        val responsesWithModificationInfo = addModificationInfoWithLatestOnly(responses)

        return AssignmentResponseSummaryDTO(
            assignmentId = assignmentId,
            questionId = question.id!!,
            questionTitle = question.title,
            questionType = question.questionType,
            totalResponses = responsesWithModificationInfo.size,
            responses = responsesWithModificationInfo
        )
    }

    /**
     * 사용자의 특정 기간 응답 조회 (같은 날 중복 답변은 최신 것만 반환)
     */
    fun getUserResponsesByDateRange(
        userId: Long,
        institutionId: Long,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): List<QuestionResponseDetailDTO> {
        logger.info { "Fetching responses for user: $userId between $startDate and $endDate" }

        // 사용자 확인
        userRepository.findByIdAndInstitutionId(userId, institutionId)
            ?: throw UserNotFoundException("User not found: $userId")

        val responses = questionResponseRepository.findByUserIdAndInstitutionIdAndDateRange(
            userId, institutionId, startDate, endDate
        )

        return addModificationInfoWithLatestOnly(responses)
    }

    /**
     * 기관의 최근 응답 조회 (같은 날 중복 답변은 최신 것만 반환)
     */
    fun getRecentResponses(institutionId: Long, limit: Int): List<QuestionResponseDetailDTO> {
        logger.info { "Fetching recent $limit responses for institution: $institutionId" }

        val pageable = org.springframework.data.domain.PageRequest.of(0, limit)
        val responses = questionResponseRepository.findRecentByInstitutionId(institutionId, pageable)

        return addModificationInfoWithLatestOnly(responses)
    }

    /**
     * 특정 질문, 사용자, 날짜의 응답 이력 조회
     */
    fun getQuestionResponseHistory(
        questionId: Long,
        userId: Long,
        date: LocalDate,
        institutionId: Long
    ): List<QuestionResponseDetailDTO> {
        logger.info { "Fetching response history for question: $questionId, user: $userId, date: $date" }

        // 사용자 확인
        userRepository.findByIdAndInstitutionId(userId, institutionId)
            ?: throw UserNotFoundException("User not found: $userId")

        val dateTime = date.atStartOfDay()
        val responses = questionResponseRepository.findByQuestionIdAndUserIdAndDateWithDetails(
            questionId, userId, institutionId, dateTime
        )

        return addModificationInfo(responses)
    }

    /**
     * 응답 목록에 수정 정보 추가 (일반 조회용 - 같은 날짜/질문/사용자의 최신 답변만 반환)
     */
    private fun addModificationInfoWithLatestOnly(responses: List<QuestionResponse>): List<QuestionResponseDetailDTO> {
        data class ResponseKey(val date: LocalDate, val questionId: Long, val userId: Long)

        // (날짜, 질문ID, 사용자ID)별로 그룹핑
        val groupedResponses = responses.groupBy { response ->
            ResponseKey(
                date = response.submittedAt.toLocalDate(),
                questionId = response.question?.id ?: 0L,
                userId = response.user?.id ?: 0L
            )
        }

        // 각 그룹에서 가장 최근 답변만 선택
        return groupedResponses.flatMap { (_, groupResponses) ->
            val modificationCount = groupResponses.size
            // submittedAt이 가장 최근인 것만 선택
            val latestResponse = groupResponses.maxByOrNull { it.submittedAt }

            if (latestResponse != null) {
                listOf(latestResponse.toDetailDTO(modificationCount))
            } else {
                emptyList()
            }
        }.sortedByDescending { it.submittedAt }  // 최신순 정렬
    }

    /**
     * 응답 목록에 수정 정보 추가 (이력 조회용 - 모든 답변 반환)
     */
    private fun addModificationInfo(responses: List<QuestionResponse>): List<QuestionResponseDetailDTO> {
        // (날짜, 질문ID, 사용자ID)별로 응답 개수 카운트
        data class ResponseKey(val date: LocalDate, val questionId: Long, val userId: Long)

        val countMap = responses.groupBy { response ->
            ResponseKey(
                date = response.submittedAt.toLocalDate(),
                questionId = response.question?.id ?: 0L,
                userId = response.user?.id ?: 0L
            )
        }.mapValues { it.value.size }

        return responses.map { response ->
            val key = ResponseKey(
                date = response.submittedAt.toLocalDate(),
                questionId = response.question?.id ?: 0L,
                userId = response.user?.id ?: 0L
            )
            val modificationCount = countMap[key] ?: 1
            response.toDetailDTO(modificationCount)
        }
    }

    /**
     * Entity를 DTO로 변환
     */
    private fun QuestionResponse.toDetailDTO(modificationCount: Int): QuestionResponseDetailDTO {
        val user = this.user ?: throw UserNotFoundException("User not found for response: ${this.id}")
        val question = this.question ?: throw QuestionNotFoundException("Question not found for response: ${this.id}")
        val assignment = this.assignment ?: throw QuestionAssignmentNotFoundException("Assignment not found for response: ${this.id}")

        return QuestionResponseDetailDTO(
            responseId = this.id!!,
            assignmentId = assignment.id!!,
            questionId = question.id!!,
            questionTitle = question.title,
            questionContent = question.content,
            questionType = question.questionType,
            userId = user.id!!,
            userName = user.name,
            responseData = this.responseData,
            responseText = this.responseText,
            otherResponse = this.otherResponse,
            responseTimeSeconds = this.responseTimeSeconds,
            submittedAt = this.submittedAt,
            ipAddress = this.ipAddress?.hostAddress,
            userAgent = this.userAgent,
            deviceInfo = this.deviceInfo,
            isModified = modificationCount > 1,
            modificationCount = modificationCount
        )
    }
}
