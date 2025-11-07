package com.onsae.api.user.dto

import java.time.LocalDateTime

data class UserGroupMemberResponse(
    val id: Long,
    val groupId: Long,
    val groupName: String,
    val userId: Long,
    val username: String,
    val userName: String,
    val joinedAt: LocalDateTime,
    val isActive: Boolean,
    val addedById: Long?,
    val addedByName: String?
)