package com.onsae.api.survey.controller

import com.onsae.api.auth.security.CustomUserPrincipal
import com.onsae.api.survey.dto.CategoryRequest
import com.onsae.api.survey.dto.CategoryResponse
import com.onsae.api.survey.service.CategoryService
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
@RequestMapping("/api/categories")
@Tag(name = "카테고리", description = "설문 카테고리 관리 API")
class CategoryController(
    private val categoryService: CategoryService
) {

    @PostMapping
    @Operation(
        summary = "카테고리 생성",
        description = "새로운 설문 카테고리를 생성합니다. 관리자 권한이 필요합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "카테고리 생성 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "권한 부족"),
            ApiResponse(responseCode = "409", description = "이미 존재하는 카테고리 이름")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    fun createCategory(
        @Valid @RequestBody request: CategoryRequest,
        authentication: Authentication
    ): ResponseEntity<CategoryResponse> {
        val principal = authentication.principal as CustomUserPrincipal

        logger.info("Category creation request by admin: ${principal.userId}")

        val response = categoryService.createCategory(request, principal.institutionId!!, principal.userId)

        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping
    @Operation(
        summary = "카테고리 목록 조회",
        description = "소속 기관의 모든 카테고리 목록을 조회합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "카테고리 목록 조회 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "권한 부족")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    fun getAllCategories(authentication: Authentication): ResponseEntity<List<CategoryResponse>> {
        val principal = authentication.principal as CustomUserPrincipal
        val categories = categoryService.getAllCategories(principal.institutionId!!)
        return ResponseEntity.ok(categories)
    }

    @GetMapping("/active")
    @Operation(
        summary = "활성 카테고리 목록 조회",
        description = "소속 기관의 활성 상태인 카테고리 목록을 조회합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "활성 카테고리 목록 조회 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "권한 부족")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    fun getActiveCategories(authentication: Authentication): ResponseEntity<List<CategoryResponse>> {
        val principal = authentication.principal as CustomUserPrincipal
        val categories = categoryService.getActiveCategories(principal.institutionId!!)
        return ResponseEntity.ok(categories)
    }

    @GetMapping("/{categoryId}")
    @Operation(
        summary = "카테고리 상세 조회",
        description = "특정 카테고리의 상세 정보를 조회합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "카테고리 조회 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "권한 부족"),
            ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없음")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    fun getCategoryById(
        @PathVariable categoryId: Long,
        authentication: Authentication
    ): ResponseEntity<CategoryResponse> {
        val principal = authentication.principal as CustomUserPrincipal
        val category = categoryService.getCategoryById(categoryId, principal.institutionId!!)
        return ResponseEntity.ok(category)
    }

    @PutMapping("/{categoryId}")
    @Operation(
        summary = "카테고리 수정",
        description = "기존 카테고리의 정보를 수정합니다. 관리자 권한이 필요합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "카테고리 수정 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "권한 부족"),
            ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없음"),
            ApiResponse(responseCode = "409", description = "이미 존재하는 카테고리 이름")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    fun updateCategory(
        @PathVariable categoryId: Long,
        @Valid @RequestBody request: CategoryRequest,
        authentication: Authentication
    ): ResponseEntity<CategoryResponse> {
        val principal = authentication.principal as CustomUserPrincipal

        logger.info("Category update request by admin: ${principal.userId} for category: $categoryId")

        val response = categoryService.updateCategory(categoryId, request, principal.institutionId!!)

        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{categoryId}")
    @Operation(
        summary = "카테고리 삭제",
        description = "카테고리를 비활성화합니다. 관리자 권한이 필요합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "카테고리 삭제 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "권한 부족"),
            ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없음")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    fun deleteCategory(
        @PathVariable categoryId: Long,
        authentication: Authentication
    ): ResponseEntity<Void> {
        val principal = authentication.principal as CustomUserPrincipal

        logger.info("Category deletion request by admin: ${principal.userId} for category: $categoryId")

        categoryService.deleteCategory(categoryId, principal.institutionId!!)

        return ResponseEntity.noContent().build()
    }
}