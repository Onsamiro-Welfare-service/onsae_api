package com.onsae.api.auth.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "관리자 회원가입 응답")
data class AdminRegisterResponse(
    @Schema(description = "등록된 관리자 ID")
    val adminId: Long,

    @Schema(description = "관리자 이름")
    val name: String,

    @Schema(description = "이메일")
    val email: String,

    @Schema(description = "역할")
    val role: String,

    @Schema(description = "기관 ID")
    val institutionId: Long,

    @Schema(description = "기관명")
    val institutionName: String,

    @Schema(description = "승인 상태")
    val status: String,

    @Schema(description = "등록 일시")
    val createdAt: LocalDateTime,

    @Schema(description = "승인 대기 메시지")
    val message: String = "관리자 등록이 완료되었습니다. 시스템 관리자의 승인을 기다려주세요."
)

@Schema(description = "사용자 등록 응답")
data class UserRegisterResponse(
    @Schema(description = "등록된 사용자 ID")
    val userId: Long,

    @Schema(description = "사용자 이름")
    val name: String,

    @Schema(description = "로그인 코드")
    val loginCode: String,

    @Schema(description = "기관 ID")
    val institutionId: Long,

    @Schema(description = "기관명")
    val institutionName: String,

    @Schema(description = "등록 일시")
    val createdAt: LocalDateTime,

    @Schema(description = "완료 메시지")
    val message: String = "사용자 등록이 완료되었습니다."
)

@Schema(description = "승인 대기 관리자 정보")
data class PendingAdminInfo(
    @Schema(description = "관리자 ID")
    val id: Long,

    @Schema(description = "관리자 이름")
    val name: String,

    @Schema(description = "이메일")
    val email: String,

    @Schema(description = "전화번호")
    val phone: String?,

    @Schema(description = "역할")
    val role: String,

    @Schema(description = "기관 ID")
    val institutionId: Long,

    @Schema(description = "기관명")
    val institutionName: String,

    @Schema(description = "등록 일시")
    val createdAt: LocalDateTime
)

@Schema(description = "관리자 승인/거부 요청")
data class AdminApprovalRequest(
    @Schema(description = "승인 여부 (true: 승인, false: 거부)")
    val approved: Boolean,

    @Schema(description = "거부 사유 (거부시 필수)")
    val rejectionReason: String?
) {
    fun validate() {
        if (!approved && rejectionReason.isNullOrBlank()) {
            throw IllegalArgumentException("거부시 거부 사유는 필수입니다")
        }
    }
}

@Schema(description = "관리자 승인/거부 응답")
data class AdminApprovalResponse(
    @Schema(description = "관리자 ID")
    val adminId: Long,

    @Schema(description = "관리자 이름")
    val name: String,

    @Schema(description = "이메일")
    val email: String,

    @Schema(description = "승인 여부")
    val approved: Boolean,

    @Schema(description = "처리 일시")
    val processedAt: LocalDateTime,

    @Schema(description = "처리자 ID")
    val processedBy: Long,

    @Schema(description = "거부 사유")
    val rejectionReason: String?,

    @Schema(description = "완료 메시지")
    val message: String
)