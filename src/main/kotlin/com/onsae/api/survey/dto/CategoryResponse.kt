package com.onsae.api.survey.dto

import java.time.LocalDateTime

data class CategoryResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val imagePath: String?,
    val isActive: Boolean,
    val institutionId: Long,
    val institutionName: String,
    val createdById: Long?,
    val createdByName: String?,
    val questionCount: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)