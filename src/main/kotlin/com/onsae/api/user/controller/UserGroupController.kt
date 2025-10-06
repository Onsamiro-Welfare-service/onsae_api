package com.onsae.api.user.controller

import com.onsae.api.auth.security.CustomUserPrincipal
import com.onsae.api.user.dto.*
import com.onsae.api.user.service.UserGroupService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/api/user-groups")
@Tag(name = "사용자 그룹", description = "사용자 그룹 관리 API")
class UserGroupController(
    private val userGroupService: UserGroupService
) {

    @PostMapping
    @Operation(
        summary = "사용자 그룹 생성",
        description = "새로운 사용자 그룹을 생성합니다. 관리자 권한이 필요합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "그룹 생성 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "권한 부족"),
            ApiResponse(responseCode = "409", description = "이미 존재하는 그룹 이름")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    fun createUserGroup(
        @Valid @RequestBody request: UserGroupRequest,
        authentication: Authentication
    ): ResponseEntity<UserGroupResponse> {
        val principal = authentication.principal as CustomUserPrincipal

        logger.info("User group creation request by admin: ${principal.userId}")

        val response = userGroupService.createUserGroup(request, principal.institutionId!!, principal.userId)

        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping
    @Operation(
        summary = "사용자 그룹 목록 조회",
        description = "소속 기관의 모든 사용자 그룹 목록을 조회합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "그룹 목록 조회 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "권한 부족")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    fun getAllUserGroups(authentication: Authentication): ResponseEntity<List<UserGroupResponse>> {
        val principal = authentication.principal as CustomUserPrincipal
        val groups = userGroupService.getAllUserGroups(principal.institutionId!!)
        return ResponseEntity.ok(groups)
    }

    @GetMapping("/active")
    @Operation(
        summary = "활성 사용자 그룹 목록 조회",
        description = "소속 기관의 활성 상태인 사용자 그룹 목록을 조회합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "활성 그룹 목록 조회 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "권한 부족")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    fun getActiveUserGroups(authentication: Authentication): ResponseEntity<List<UserGroupResponse>> {
        val principal = authentication.principal as CustomUserPrincipal
        val groups = userGroupService.getActiveUserGroups(principal.institutionId!!)
        return ResponseEntity.ok(groups)
    }

    @GetMapping("/{groupId}")
    @Operation(
        summary = "사용자 그룹 상세 조회",
        description = "특정 사용자 그룹의 상세 정보를 조회합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "그룹 조회 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "권한 부족"),
            ApiResponse(responseCode = "404", description = "그룹을 찾을 수 없음")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    fun getUserGroupById(
        @PathVariable groupId: Long,
        authentication: Authentication
    ): ResponseEntity<UserGroupResponse> {
        val principal = authentication.principal as CustomUserPrincipal
        val group = userGroupService.getUserGroupById(groupId, principal.institutionId!!)
        return ResponseEntity.ok(group)
    }

    @PutMapping("/{groupId}")
    @Operation(
        summary = "사용자 그룹 수정",
        description = "기존 사용자 그룹의 정보를 수정합니다. 관리자 권한이 필요합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "그룹 수정 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "권한 부족"),
            ApiResponse(responseCode = "404", description = "그룹을 찾을 수 없음"),
            ApiResponse(responseCode = "409", description = "이미 존재하는 그룹 이름")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    fun updateUserGroup(
        @PathVariable groupId: Long,
        @Valid @RequestBody request: UserGroupRequest,
        authentication: Authentication
    ): ResponseEntity<UserGroupResponse> {
        val principal = authentication.principal as CustomUserPrincipal

        logger.info("User group update request by admin: ${principal.userId} for group: $groupId")

        val response = userGroupService.updateUserGroup(groupId, request, principal.institutionId!!)

        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{groupId}")
    @Operation(
        summary = "사용자 그룹 삭제",
        description = "사용자 그룹을 비활성화합니다. 관리자 권한이 필요합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "그룹 삭제 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "권한 부족"),
            ApiResponse(responseCode = "404", description = "그룹을 찾을 수 없음")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    fun deleteUserGroup(
        @PathVariable groupId: Long,
        authentication: Authentication
    ): ResponseEntity<Void> {
        val principal = authentication.principal as CustomUserPrincipal

        logger.info("User group deletion request by admin: ${principal.userId} for group: $groupId")

        userGroupService.deleteUserGroup(groupId, principal.institutionId!!)

        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{groupId}/members")
    @Operation(
        summary = "그룹에 멤버 추가",
        description = "사용자 그룹에 새로운 멤버들을 추가합니다. 관리자 권한이 필요합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "멤버 추가 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "권한 부족"),
            ApiResponse(responseCode = "404", description = "그룹 또는 사용자를 찾을 수 없음")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    fun addMembersToGroup(
        @PathVariable groupId: Long,
        @Valid @RequestBody request: UserGroupMemberRequest,
        authentication: Authentication
    ): ResponseEntity<List<UserGroupMemberResponse>> {
        val principal = authentication.principal as CustomUserPrincipal

        logger.info("Adding members to group: $groupId by admin: ${principal.userId}")

        val members = userGroupService.addMembersToGroup(groupId, request, principal.institutionId!!, principal.userId)

        return ResponseEntity.status(HttpStatus.CREATED).body(members)
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    @Operation(
        summary = "그룹에서 멤버 제거",
        description = "사용자 그룹에서 특정 멤버를 제거합니다. 관리자 권한이 필요합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "멤버 제거 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "권한 부족"),
            ApiResponse(responseCode = "404", description = "그룹 또는 멤버를 찾을 수 없음")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    fun removeMemberFromGroup(
        @PathVariable groupId: Long,
        @PathVariable userId: Long,
        authentication: Authentication
    ): ResponseEntity<Void> {
        val principal = authentication.principal as CustomUserPrincipal

        logger.info("Removing user $userId from group: $groupId by admin: ${principal.userId}")

        userGroupService.removeMemberFromGroup(groupId, userId, principal.institutionId!!)

        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{groupId}/members")
    @Operation(
        summary = "그룹 멤버 목록 조회",
        description = "사용자 그룹의 모든 멤버 목록을 조회합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "멤버 목록 조회 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "권한 부족"),
            ApiResponse(responseCode = "404", description = "그룹을 찾을 수 없음")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    fun getGroupMembers(
        @PathVariable groupId: Long,
        authentication: Authentication
    ): ResponseEntity<List<UserGroupMemberResponse>> {
        val principal = authentication.principal as CustomUserPrincipal
        val members = userGroupService.getGroupMembers(groupId, principal.institutionId!!)
        return ResponseEntity.ok(members)
    }
}