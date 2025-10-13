package com.onsae.api.institution.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "기관 상세 정보 응답")
data class InstitutionDetailResponse(
    @Schema(description = "기관 ID", example = "1")
    val id: Long,

    @Schema(description = "기관명", example = "온새 복지관")
    val name: String,

    @Schema(description = "사업자등록번호", example = "123-45-67890")
    val businessNumber: String?,

    @Schema(description = "사회복지법인 등록번호", example = "서울-2023-001")
    val registrationNumber: String?,

    @Schema(description = "주소", example = "서울시 강남구 테헤란로 123")
    val address: String?,

    @Schema(description = "대표 전화번호", example = "02-1234-5678")
    val phone: String?,

    @Schema(description = "대표 이메일", example = "info@onsae.com")
    val email: String?,

    @Schema(description = "원장명", example = "김원장")
    val directorName: String?,

    @Schema(description = "웹사이트", example = "https://onsae.com")
    val website: String?,

    @Schema(description = "담당자명", example = "이담당")
    val contactPerson: String?,

    @Schema(description = "담당자 전화번호", example = "010-1234-5678")
    val contactPhone: String?,

    @Schema(description = "담당자 이메일", example = "contact@onsae.com")
    val contactEmail: String?,

    @Schema(description = "활성화 상태", example = "true")
    val isActive: Boolean,

    @Schema(description = "타임존", example = "Asia/Seoul")
    val timezone: String,

    @Schema(description = "로케일", example = "ko_KR")
    val locale: String,

    @Schema(description = "생성일시")
    val createdAt: LocalDateTime,

    @Schema(description = "수정일시")
    val updatedAt: LocalDateTime
)