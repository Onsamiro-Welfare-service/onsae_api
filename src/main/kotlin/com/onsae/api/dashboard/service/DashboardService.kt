package com.onsae.api.dashboard.service

import com.onsae.api.dashboard.dto.*
import com.onsae.api.file.entity.Upload
import com.onsae.api.file.repository.UploadRepository
import com.onsae.api.survey.entity.QuestionResponse
import com.onsae.api.survey.repository.QuestionAssignmentRepository
import com.onsae.api.survey.repository.QuestionResponseRepository
import com.onsae.api.user.entity.User
import com.onsae.api.user.repository.UserGroupMemberRepository
import com.onsae.api.user.repository.UserGroupRepository
import com.onsae.api.user.repository.UserRepository
import mu.KotlinLogging
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.math.roundToInt

private val logger = KotlinLogging.logger {}

@Service
@Transactional(readOnly = true)
class DashboardService(
    private val userRepository: UserRepository,
    private val questionResponseRepository: QuestionResponseRepository,
    private val questionAssignmentRepository: QuestionAssignmentRepository,
    private val uploadRepository: UploadRepository,
    private val userGroupRepository: UserGroupRepository,
    private val userGroupMemberRepository: UserGroupMemberRepository
) {

    fun getDashboardStats(institutionId: Long): DashboardStatsResponse {
        logger.info("Fetching dashboard stats for institution: $institutionId")

        val now = LocalDateTime.now()
        val today = now.toLocalDate()
        val weekAgo = now.minusWeeks(1)

        // 총 사용자 수 및 활성 사용자 수
        val totalUsers = userRepository.countByInstitutionId(institutionId)
        val activeUsers = userRepository.countByInstitutionIdAndIsActiveTrue(institutionId)

        // 지난주 대비 변화량
        val totalUsersLastWeek = userRepository.countByInstitutionIdAndCreatedAtBefore(institutionId, weekAgo)
        val totalUsersChange = totalUsers - totalUsersLastWeek

        val activeUsersLastWeek = userRepository.countByInstitutionIdAndIsActiveTrueAndLastLoginBefore(institutionId, weekAgo)
        val activeUsersChange = activeUsers - activeUsersLastWeek

        // 오늘 응답 현황
        val todayStart = today.atStartOfDay()
        val todayEnd = today.plusDays(1).atStartOfDay()

        val todayResponses = questionResponseRepository.countByInstitutionIdAndSubmittedAtBetween(
            institutionId, todayStart, todayEnd
        )

        val todayAssignments = questionAssignmentRepository.countActiveByInstitutionIdAndDate(
            institutionId, today
        )

        val todayResponseRate = if (todayAssignments > 0) {
            (todayResponses.toDouble() / todayAssignments * 100)
        } else 0.0

        val yesterdayStart = today.minusDays(1).atStartOfDay()
        val yesterdayResponses = questionResponseRepository.countByInstitutionIdAndSubmittedAtBetween(
            institutionId, yesterdayStart, todayStart
        )
        val todayResponsesChange = todayResponses - yesterdayResponses

        // 미처리 업로드
        val pendingUploads = uploadRepository.countByInstitutionIdAndProcessedAtIsNull(institutionId)
        val yesterdayPendingUploads = uploadRepository.countByInstitutionIdAndProcessedAtIsNullAndCreatedAtBefore(
            institutionId, yesterdayStart
        )
        val pendingUploadsChange = pendingUploads - yesterdayPendingUploads

        return DashboardStatsResponse(
            totalUsers = totalUsers,
            activeUsers = activeUsers,
            totalUsersChange = ChangeInfo(value = totalUsersChange, period = "이번 주"),
            activeUsersChange = ChangeInfo(value = activeUsersChange, period = "이번 주"),
            todayResponses = TodayResponseInfo(
                total = todayResponses,
                assigned = todayAssignments,
                rate = todayResponseRate.roundToInt().toDouble(),
                change = todayResponsesChange
            ),
            pendingUploads = PendingInfo(
                count = pendingUploads,
                change = pendingUploadsChange
            ),
            pendingApprovals = ApprovalInfo(
                admins = 0, // TODO: 실제 승인 대기 수 계산
                welfareCenters = 0
            )
        )
    }

    fun getResponseTrends(institutionId: Long, period: String): ResponseTrendsResponse {
        logger.info("Fetching response trends for institution: $institutionId, period: $period")

        val days = when (period) {
            "30d" -> 30
            "90d" -> 90
            else -> 7 // default 7d
        }

        val today = LocalDate.now()
        val startDate = today.minusDays(days.toLong() - 1)

        val data = mutableListOf<DailyResponseData>()
        var totalAssignedQuestions = 0
        var totalCompletedResponses = 0
        var totalRate = 0.0

        for (i in 0 until days) {
            val date = startDate.plusDays(i.toLong())
            val dayStart = date.atStartOfDay()
            val dayEnd = date.plusDays(1).atStartOfDay()

            val responses = questionResponseRepository.findByInstitutionIdAndSubmittedAtBetween(
                institutionId, dayStart, dayEnd
            )

            val completedResponses = responses.filter { it.responseData.isNotEmpty() }.size
            val assignmentsCount = questionAssignmentRepository.countActiveByInstitutionIdAndDate(
                institutionId, date
            )

            val responseRate = if (assignmentsCount > 0) {
                (completedResponses.toDouble() / assignmentsCount * 100)
            } else 0.0

            val byCategory = responses.groupBy { response ->
                response.question?.category?.name ?: "기타"
            }.mapValues { it.value.size }

            data.add(
                DailyResponseData(
                    date = date,
                    assignedQuestions = assignmentsCount,
                    completedResponses = completedResponses,
                    responseRate = responseRate,
                    byCategory = byCategory
                )
            )

            totalAssignedQuestions += assignmentsCount
            totalCompletedResponses += completedResponses
            totalRate += responseRate
        }

        val avgResponseRate = if (days > 0) totalRate / days else 0.0
        val trend = if (data.size >= 2) {
            when {
                data.last().responseRate > data.first().responseRate -> "up"
                data.last().responseRate < data.first().responseRate -> "down"
                else -> "stable"
            }
        } else "stable"

        return ResponseTrendsResponse(
            period = period,
            data = data,
            summary = TrendSummary(
                avgResponseRate = avgResponseRate,
                totalAssignedQuestions = totalAssignedQuestions,
                totalCompletedResponses = totalCompletedResponses,
                trend = trend
            )
        )
    }

    fun getUserGroups(institutionId: Long): UserGroupsResponse {
        logger.info("Fetching user groups for institution: $institutionId")

        val groups = userGroupRepository.findByInstitutionId(institutionId)
        val colors = listOf("#FF6B6B", "#4ECDC4", "#95E1D3", "#FFD93D", "#6BCB77", "#4D96FF", "#FF8DCE")

        val groupInfos = groups.mapIndexed { index, group ->
            val memberCount = userGroupMemberRepository.countByGroupId(group.id!!)
            val activeMembers = userGroupMemberRepository.countActiveByGroupId(group.id!!)

            val assignedQuestions = questionAssignmentRepository.countByGroupId(group.id!!)
            val completedResponses = questionResponseRepository.countCompletedByGroupId(group.id!!)

            val responseRate = if (assignedQuestions > 0) {
                (completedResponses.toDouble() / assignedQuestions * 100)
            } else 0.0

            GroupInfo(
                groupId = group.id!!,
                groupName = group.name,
                memberCount = memberCount,
                activeMembers = activeMembers,
                assignedQuestions = assignedQuestions,
                completedResponses = completedResponses,
                responseRate = responseRate,
                color = colors[index % colors.size]
            )
        }

        // 사용자별 그룹 소속 현황 계산
        val totalMembers = userRepository.countByInstitutionId(institutionId)
        val allUsers = userRepository.findByInstitutionId(institutionId)
        val userGroupMembers = userGroupMemberRepository.findByInstitutionId(institutionId)

        // 그룹 ID -> 그룹 이름 매핑
        val groupIdToName = groups.associate { it.id!! to it.name }
        val groupIdToColor = groups.mapIndexed { index, group ->
            group.id!! to colors[index % colors.size]
        }.toMap()

        // 사용자별 그룹 조합 계산
        val userToGroups = userGroupMembers
            .filter { it.isActive }
            .groupBy { it.user.id }
            .mapValues { (_, members) ->
                members.map { it.group.id!! }.sorted()
            }

        // 그룹 조합별로 사용자 수 집계
        val groupCombinationCounts = mutableMapOf<List<Long>, Int>()

        allUsers.forEach { user ->
            val userGroups = userToGroups[user.id] ?: emptyList()
            groupCombinationCounts[userGroups] = groupCombinationCounts.getOrDefault(userGroups, 0) + 1
        }

        // UserCategory 생성
        val categories = groupCombinationCounts.map { (groupIds, count) ->
            val groupNames = groupIds.mapNotNull { groupIdToName[it] }
            val label = if (groupIds.isEmpty()) {
                "그룹 미소속"
            } else {
                groupNames.joinToString("+")
            }

            // 색상 결정: 단일 그룹이면 해당 그룹 색, 복수면 혼합 색상
            val color = when {
                groupIds.isEmpty() -> "#E0E0E0"
                groupIds.size == 1 -> groupIdToColor[groupIds[0]] ?: "#CCCCCC"
                else -> generateMixedColor(groupIds.mapNotNull { groupIdToColor[it] })
            }

            UserCategory(
                groupIds = groupIds,
                groupNames = groupNames,
                userCount = count,
                label = label,
                color = color
            )
        }.sortedByDescending { it.userCount }

        val userDistribution = UserDistribution(categories = categories)

        return UserGroupsResponse(
            groups = groupInfos,
            totalMembers = totalMembers,
            userDistribution = userDistribution
        )
    }

    private fun generateMixedColor(colors: List<String>): String {
        // 복수 그룹의 경우 혼합 색상 생성 (간단히 첫 번째 색상에 투명도 적용)
        if (colors.isEmpty()) return "#CCCCCC"

        // 여러 색상을 평균내어 혼합
        val rgbValues = colors.map { hexToRgb(it) }
        val avgR = rgbValues.map { it[0] }.average().toInt()
        val avgG = rgbValues.map { it[1] }.average().toInt()
        val avgB = rgbValues.map { it[2] }.average().toInt()

        return rgbToHex(avgR, avgG, avgB)
    }

    private fun hexToRgb(hex: String): IntArray {
        val cleanHex = hex.removePrefix("#")
        return intArrayOf(
            cleanHex.substring(0, 2).toInt(16),
            cleanHex.substring(2, 4).toInt(16),
            cleanHex.substring(4, 6).toInt(16)
        )
    }

    private fun rgbToHex(r: Int, g: Int, b: Int): String {
        return "#%02X%02X%02X".format(r, g, b)
    }

    fun getRecentActivities(institutionId: Long, limit: Int, type: String?): RecentActivitiesResponse {
        logger.info("Fetching recent activities for institution: $institutionId, limit: $limit, type: $type")

        val activities = mutableListOf<ActivityInfo>()

        // 최근 응답 활동
        if (type == null || type == "all" || type == "responses") {
            val recentResponses = questionResponseRepository.findRecentByInstitutionId(
                institutionId,
                PageRequest.of(0, limit)
            )
            recentResponses.forEach { response ->
                activities.add(
                    ActivityInfo(
                        id = "resp_${response.id}",
                        type = "response",
                        user = UserBasicInfo(
                            id = response.user?.id ?: 0,
                            name = response.user?.name ?: "Unknown",
                            code = response.user?.username ?: ""
                        ),
                        question = response.question?.let {
                            QuestionBasicInfo(
                                id = it.id ?: 0,
                                title = it.title
                            )
                        },
                        upload = null,
                        timestamp = response.submittedAt,
                        status = if (response.responseData.isNotEmpty()) "completed" else "incomplete",
                        priority = "normal"
                    )
                )
            }
        }

        // 최근 업로드 활동
        if (type == null || type == "all" || type == "uploads") {
            val recentUploads = uploadRepository.findRecentByInstitutionId(
                institutionId,
                PageRequest.of(0, limit)
            )
            recentUploads.forEach { upload ->
                activities.add(
                    ActivityInfo(
                        id = "upload_${upload.id}",
                        type = "upload",
                        user = UserBasicInfo(
                            id = upload.user?.id ?: 0,
                            name = upload.user?.name ?: "Unknown",
                            code = upload.user?.username ?: ""
                        ),
                        question = null,
                        upload = UploadBasicInfo(
                            id = upload.id ?: 0,
                            type = upload.title ?: "unknown",
                            fileName = upload.files.firstOrNull()?.fileName ?: "unknown"
                        ),
                        timestamp = upload.createdAt,
                        status = if (upload.adminResponseDate != null) "completed" else "pending",
                        priority = if (upload.adminResponseDate == null) "high" else "normal"
                    )
                )
            }
        }

        // 타임스탬프 기준 정렬
        activities.sortByDescending { it.timestamp }
        val limitedActivities = activities.take(limit)

        return RecentActivitiesResponse(
            activities = limitedActivities,
            pagination = PaginationInfo(
                total = activities.size,
                page = 1,
                limit = limit
            )
        )
    }
}
