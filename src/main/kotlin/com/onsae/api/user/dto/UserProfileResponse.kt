package com.onsae.api.user.dto

import com.onsae.api.common.entity.SeverityLevel
import java.time.LocalDate
import java.time.LocalDateTime

data class UserProfileResponse(
    val id: Long,
    val usercode: String,
    val name: String,
    val phone: String?,
    val address: String?,
    val birthDate: LocalDate?,
    val severity: SeverityLevel,
    val guardianName: String?,
    val guardianRelationship: String?,
    val guardianPhone: String?,
    val guardianEmail: String?,
    val guardianAddress: String?,
    val emergencyContacts: Map<String, Any>?,
    val careNotes: String?,
    val isActive: Boolean,
    val lastLogin: LocalDateTime?,
    val institutionId: Long,
    val institutionName: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)