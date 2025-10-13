package com.onsae.api.institution.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Size

@Schema(description = "기관 수정 요청")
data class InstitutionUpdateRequest(
    @field:Size(max = 30, message = "기관명은 30자 이내여야 합니다")
    @Schema(description = "기관명", example = "온새 복지관")
    val name: String?,

    @field:Size(max = 12, message = "사업자등록번호는 하이픈(-) 포함 12자 이내여야 합니다")
    @Schema(description = "사업자등록번호", example = "123-45-67890")
    val businessNumber: String?,

    @field:Size(max = 50, message = "사회복지법인 등록번호는 50자 이내여야 합니다")
    @Schema(description = "사회복지법인 등록번호", example = "서울-2023-001")
    val registrationNumber: String?,

    @Schema(description = "주소", example = "서울시 강남구 테헤란로 123")
    val address: String?,

    @field:Size(max = 13, message = "전화번호는 하이픈(-) 포함 13자 이내여야 합니다")
    @Schema(description = "대표 전화번호", example = "02-1234-5678")
    val phone: String?,

    @field:Email(message = "올바른 이메일 형식이 아닙니다")
    @field:Size(max = 100, message = "이메일은 100자 이내여야 합니다")
    @Schema(description = "대표 이메일", example = "info@onsae.com")
    val email: String?,

    @field:Size(max = 10, message = "원장명은 10자 이내여야 합니다")
    @Schema(description = "원장명", example = "김원장")
    val directorName: String?,

    @field:Size(max = 200, message = "웹사이트는 200자 이내여야 합니다")
    @Schema(description = "웹사이트", example = "https://onsae.com")
    val website: String?,

    @field:Size(max = 10, message = "담당자명은 10자 이내여야 합니다")
    @Schema(description = "담당자명", example = "이담당")
    val contactPerson: String?,

    @field:Size(max = 13, message = "담당자 전화번호는 하이픈(-) 포함 13자 이내여야 합니다")
    @Schema(description = "담당자 전화번호", example = "010-1234-5678")
    val contactPhone: String?,

    @field:Email(message = "올바른 이메일 형식이 아닙니다")
    @field:Size(max = 100, message = "담당자 이메일은 100자 이내여야 합니다")
    @Schema(description = "담당자 이메일", example = "contact@onsae.com")
    val contactEmail: String?,

    @Schema(description = "활성화 상태", example = "true")
    val isActive: Boolean?
)