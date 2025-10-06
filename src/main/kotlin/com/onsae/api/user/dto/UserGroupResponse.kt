package com.onsae.api.user.dto

import java.time.LocalDateTime

data class UserGroupResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val isActive: Boolean,
    val memberCount: Int,
    val institutionId: Long,
    val institutionName: String,
    val createdById: Long?,
    val createdByName: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)