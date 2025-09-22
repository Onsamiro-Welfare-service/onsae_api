package com.onsae.api.auth.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDate

@Schema(description = "관리자 회원가입 요청")
data class AdminRegisterRequest(
    @field:NotBlank(message = "이름은 필수입니다")
    @field:Size(max = 50, message = "이름은 50자 이내여야 합니다")
    @Schema(description = "관리자 이름", example = "홍길동")
    val name: String,

    @field:NotBlank(message = "이메일은 필수입니다")
    @field:Email(message = "올바른 이메일 형식이 아닙니다")
    @field:Size(max = 100, message = "이메일은 100자 이내여야 합니다")
    @Schema(description = "이메일", example = "admin@example.com")
    val email: String,

    @field:NotBlank(message = "비밀번호는 필수입니다")
    @field:Size(min = 8, max = 100, message = "비밀번호는 8자 이상 100자 이내여야 합니다")
    @Schema(description = "비밀번호", example = "password123!")
    val password: String,

    @field:Size(max = 20, message = "전화번호는 20자 이내여야 합니다")
    @Schema(description = "전화번호", example = "010-1234-5678")
    val phone: String?,

    @field:NotNull(message = "역할은 필수입니다")
    @Schema(description = "관리자 역할")
    val role: AdminRole,

    @Schema(description = "기존 기관 ID (기존 기관에 소속될 경우)")
    val institutionId: Long?,

    @field:Valid
    @Schema(description = "새 기관 정보 (새 기관을 등록할 경우)")
    val institutionInfo: InstitutionRegisterRequest?
) {
    fun validate() {
        if (institutionId == null && institutionInfo == null) {
            throw IllegalArgumentException("기관 ID 또는 새 기관 정보 중 하나는 필수입니다")
        }
        if (institutionId != null && institutionInfo != null) {
            throw IllegalArgumentException("기관 ID와 새 기관 정보를 동시에 제공할 수 없습니다")
        }
    }
}

@Schema(description = "관리자 역할")
enum class AdminRole {
    @Schema(description = "관리자")
    ADMIN,

    @Schema(description = "직원")
    STAFF
}

@Schema(description = "기관 등록 요청")
data class InstitutionRegisterRequest(
    @field:NotBlank(message = "기관명은 필수입니다")
    @field:Size(max = 200, message = "기관명은 200자 이내여야 합니다")
    @Schema(description = "기관명", example = "온새 복지관")
    val name: String,

    @field:Size(max = 50, message = "사업자등록번호는 50자 이내여야 합니다")
    @Schema(description = "사업자등록번호", example = "123-45-67890")
    val businessNumber: String?,

    @Schema(description = "주소", example = "서울시 강남구 테헤란로 123")
    val address: String?,

    @field:Size(max = 20, message = "전화번호는 20자 이내여야 합니다")
    @Schema(description = "대표 전화번호", example = "02-1234-5678")
    val phone: String?,

    @field:Email(message = "올바른 이메일 형식이 아닙니다")
    @field:Size(max = 100, message = "이메일은 100자 이내여야 합니다")
    @Schema(description = "대표 이메일", example = "info@onsae.com")
    val email: String?,

    @field:Size(max = 100, message = "원장명은 100자 이내여야 합니다")
    @Schema(description = "원장명", example = "김원장")
    val directorName: String?,

    @field:Size(max = 200, message = "웹사이트는 200자 이내여야 합니다")
    @Schema(description = "웹사이트", example = "https://onsae.com")
    val website: String?
)

@Schema(description = "사용자 등록 요청")
data class UserRegisterRequest(
    @field:NotBlank(message = "이름은 필수입니다")
    @field:Size(max = 50, message = "이름은 50자 이내여야 합니다")
    @Schema(description = "사용자 이름", example = "홍길동")
    val name: String,

    @field:NotBlank(message = "로그인 코드는 필수입니다")
    @field:Size(min = 3, max = 20, message = "로그인 코드는 3자 이상 20자 이내여야 합니다")
    @Schema(description = "로그인 코드", example = "USER001")
    val loginCode: String,

    @field:Size(max = 20, message = "전화번호는 20자 이내여야 합니다")
    @Schema(description = "전화번호", example = "010-1234-5678")
    val phone: String?,

    @Schema(description = "생년월일", example = "1990-01-01")
    val birthDate: LocalDate?,

    @Schema(description = "사용자 그룹 ID")
    val groupId: Long?
)