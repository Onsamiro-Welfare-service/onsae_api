package com.onsae.api.user.dto

import jakarta.validation.constraints.NotEmpty

data class UserGroupMemberRequest(
    @field:NotEmpty(message = "사용자 ID 목록은 필수입니다")
    val userIds: List<Long>
)