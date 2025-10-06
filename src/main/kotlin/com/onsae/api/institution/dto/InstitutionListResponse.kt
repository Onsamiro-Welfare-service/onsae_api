package com.onsae.api.institution.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "기관 목록 응답")
data class InstitutionListResponse(
    @Schema(description = "기관 ID", example = "1")
    val id: Long,

    @Schema(description = "기관명", example = "온새 복지관")
    val name: String,

    @Schema(description = "사업자등록번호", example = "123-45-67890")
    val businessNumber: String?,

    @Schema(description = "주소", example = "서울시 강남구 테헤란로 123")
    val address: String?,

    @Schema(description = "대표 전화번호", example = "02-1234-5678")
    val phone: String?,

    @Schema(description = "원장명", example = "김원장")
    val directorName: String?
)