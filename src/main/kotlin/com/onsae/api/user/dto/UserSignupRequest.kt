package com.onsae.api.user.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDate

@Schema(description = "사용자 직접 회원가입 요청")
data class UserSignupRequest(
    @field:NotNull(message = "기관 ID는 필수입니다")
    @Schema(description = "가입할 기관 ID", example = "1")
    val institutionId: Long,

    @field:NotBlank(message = "사용자명은 필수입니다")
    @field:Size(min = 3, max = 100, message = "사용자명은 3자 이상 100자 이내여야 합니다")
    @Schema(description = "사용자명 (로그인 ID)", example = "user001")
    val username: String,

    @field:NotBlank(message = "비밀번호는 필수입니다")
    @field:Size(min = 4, max = 50, message = "비밀번호는 4자 이상 50자 이내여야 합니다")
    @Schema(description = "비밀번호", example = "password123!")
    val password: String,

    @field:NotBlank(message = "이름은 필수입니다")
    @field:Size(max = 50, message = "이름은 50자 이내여야 합니다")
    @Schema(description = "사용자 이름", example = "홍길동")
    val name: String,

    @field:Size(max = 20, message = "전화번호는 20자 이내여야 합니다")
    @Schema(description = "전화번호", example = "010-1234-5678")
    val phone: String?,

    @Schema(description = "생년월일", example = "1990-01-01")
    val birthDate: LocalDate?
)
