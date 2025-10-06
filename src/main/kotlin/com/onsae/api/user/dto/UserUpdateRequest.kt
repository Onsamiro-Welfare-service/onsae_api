package com.onsae.api.user.dto

import com.onsae.api.common.entity.SeverityLevel
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDate

data class UserUpdateRequest(
    @field:NotBlank(message = "이름은 필수입니다")
    @field:Size(max = 50, message = "이름은 50자 이하여야 합니다")
    val name: String,

    @field:Size(max = 20, message = "전화번호는 20자 이하여야 합니다")
    val phone: String?,

    val address: String?,

    val birthDate: LocalDate?,

    val severity: SeverityLevel,

    @field:Size(max = 50, message = "보호자 이름은 50자 이하여야 합니다")
    val guardianName: String?,

    @field:Size(max = 20, message = "보호자 관계는 20자 이하여야 합니다")
    val guardianRelationship: String?,

    @field:Size(max = 20, message = "보호자 전화번호는 20자 이하여야 합니다")
    val guardianPhone: String?,

    @field:Email(message = "올바른 이메일 형식이어야 합니다")
    @field:Size(max = 100, message = "보호자 이메일은 100자 이하여야 합니다")
    val guardianEmail: String?,

    val guardianAddress: String?,

    val emergencyContacts: Map<String, Any>?,

    val careNotes: String?
)