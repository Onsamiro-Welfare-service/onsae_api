package com.onsae.api.user.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDate

@Schema(description = "사용자 등록 요청 (관리자용)")
data class UserRegisterRequest(
    @field:NotBlank(message = "이름은 필수입니다")
    @field:Size(max = 50, message = "이름은 50자 이내여야 합니다")
    @Schema(description = "사용자 이름", example = "홍길동")
    val name: String,

    @field:NotBlank(message = "사용자명은 필수입니다")
    @field:Size(min = 3, max = 100, message = "사용자명은 3자 이상 100자 이내여야 합니다")
    @Schema(description = "사용자명", example = "user001")
    val username: String,

    @field:NotBlank(message = "비밀번호는 필수입니다")
    @field:Size(min = 4, max = 50, message = "비밀번호는 4자 이상 50자 이내여야 합니다")
    @Schema(description = "비밀번호", example = "password123!")
    val password: String,

    @field:Size(max = 20, message = "전화번호는 20자 이내여야 합니다")
    @Schema(description = "전화번호", example = "010-1234-5678")
    val phone: String?,

    @Schema(description = "생년월일", example = "1990-01-01")
    val birthDate: LocalDate?,

    @Schema(description = "사용자 그룹 ID 목록")
    val groupIds: List<Long>? = null
)
