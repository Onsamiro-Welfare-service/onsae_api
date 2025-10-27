package com.onsae.api.user.dto

import com.onsae.api.common.entity.SeverityLevel
import java.time.LocalDate
import java.time.LocalDateTime

data class UserListResponse(
    val id: Long,
    val username: String,
    val name: String,
    val phone: String?,
    val birthDate: LocalDate?,
    val severity: SeverityLevel,
    val guardianName: String?,
    val guardianPhone: String?,
    val isActive: Boolean,
    val lastLogin: LocalDateTime?,
    val institutionId: Long,
    val institutionName: String,
    val createdAt: LocalDateTime,
    val groupIds: List<Long>
)
