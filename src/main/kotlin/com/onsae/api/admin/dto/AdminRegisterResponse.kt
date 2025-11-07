package com.onsae.api.admin.dto

import com.onsae.api.common.entity.AccountStatus
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
    val status: AccountStatus,

    @Schema(description = "등록 일시")
    val createdAt: LocalDateTime,

    @Schema(description = "승인 대기 메시지")
    val message: String = "관리자 등록이 완료되었습니다. 시스템 관리자의 승인을 기다려주세요."
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

@Schema(description = "관리자 상태 변경 요청")
data class AdminStatusChangeRequest(
    @Schema(description = "변경할 상태 (APPROVED, SUSPENDED)")
    val status: AccountStatus,

    @Schema(description = "변경 사유")
    val reason: String?
) {
    fun validate() {
        if (status !in listOf(AccountStatus.APPROVED, AccountStatus.SUSPENDED)) {
            throw IllegalArgumentException("유효하지 않은 상태입니다. APPROVED 또는 SUSPENDED만 가능합니다")
        }
        if (status == AccountStatus.SUSPENDED && reason.isNullOrBlank()) {
            throw IllegalArgumentException("계정 정지시 사유는 필수입니다")
        }
    }
}

@Schema(description = "관리자 상태 변경 응답")
data class AdminStatusChangeResponse(
    @Schema(description = "관리자 ID")
    val adminId: Long,

    @Schema(description = "관리자 이름")
    val name: String,

    @Schema(description = "이메일")
    val email: String,

    @Schema(description = "변경된 상태")
    val status: AccountStatus,

    @Schema(description = "처리 일시")
    val processedAt: LocalDateTime,

    @Schema(description = "처리자 ID")
    val processedBy: Long,

    @Schema(description = "변경 사유")
    val reason: String?,

    @Schema(description = "완료 메시지")
    val message: String
)

@Schema(description = "전체 관리자 정보")
data class AdminListInfo(
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

    @Schema(description = "계정 상태")
    val status: AccountStatus,

    @Schema(description = "등록 일시")
    val createdAt: LocalDateTime,

    @Schema(description = "마지막 로그인 일시")
    val lastLogin: LocalDateTime?,

    @Schema(description = "승인/거부 일시")
    val approvedAt: LocalDateTime?,

    @Schema(description = "승인/거부 처리자 (시스템 관리자명)")
    val approvedBy: String?,

    @Schema(description = "거부 사유")
    val rejectionReason: String?
)