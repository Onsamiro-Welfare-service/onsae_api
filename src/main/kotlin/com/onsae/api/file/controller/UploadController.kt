package com.onsae.api.file.controller

import com.onsae.api.auth.exception.InvalidCredentialsException
import com.onsae.api.auth.security.CustomUserPrincipal
import com.onsae.api.file.dto.AdminResponseRequest
import com.onsae.api.file.dto.UploadListResponse
import com.onsae.api.file.dto.UploadRequest
import com.onsae.api.file.dto.UploadResponse
import com.onsae.api.file.service.UploadService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

/**
 * 업로드 컨트롤러 (사용자용)
 * 
 * 일반 사용자가 파일을 업로드하고 자신의 업로드 내역을 조회하는 API를 제공합니다.
 * 
 * 주요 기능:
 * - 파일 업로드: POST /api/user/uploads
 * - 내 업로드 목록 조회: GET /api/user/uploads
 * - 업로드 상세 조회: GET /api/user/uploads/{id}
 */
private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/api/user/uploads")
@Tag(name = "업로드 (사용자)", description = "일반 사용자 업로드 관련 API")
class UploadController(
    private val uploadService: UploadService
) {

    /**
     * 파일 또는 텍스트를 업로드합니다.
     * 
     * @param title 업로드 제목 (선택사항)
     * @param content 업로드 내용 (선택사항, files와 함께 사용 가능)
     * @param files 업로드할 파일들 (선택사항, content와 함께 사용 가능)
     * @param authentication 인증 정보 (Spring Security에서 자동 주입)
     * @return 업로드 정보 응답
     */
    @PostMapping(
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @Operation(
        summary = "파일 또는 텍스트 업로드",
        description = "사용자가 파일 또는 텍스트를 업로드합니다. content와 files 중 하나 이상은 필수입니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "업로드 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (content와 files 모두 없음, 파일 타입 오류 등)"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "413", description = "파일 크기 초과")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    fun uploadFiles(
        @RequestParam(value = "title", required = false) title: String?,
        @RequestParam(value = "content", required = false) content: String?,
        @RequestParam(value = "files", required = false) files: List<MultipartFile>?,
        authentication: Authentication
    ): ResponseEntity<*> {
        // 안전한 캐스팅: principal이 CustomUserPrincipal 타입이 아닌 경우 null 반환
        val principal = authentication.principal as? CustomUserPrincipal
            ?: run {
                logger.error("Invalid principal type: ${authentication.principal?.javaClass?.name}")
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(mapOf(
                        "message" to "인증 정보가 올바르지 않습니다",
                        "code" to "INVALID_AUTHENTICATION"
                    ))
            }

        // 빈 파일 필터링 (MultipartFile이 비어있거나 null인 경우 제거)
        val validFiles = files?.filter { !it.isEmpty } ?: emptyList()
        
        // API 규칙 검증: content와 files 중 하나 이상은 필수
        // 불필요한 처리를 방지하기 위해 서비스 호출 전에 검증
        if (content.isNullOrBlank() && validFiles.isEmpty()) {
            logger.warn("Upload request rejected: both content and files are empty, userId: ${principal.userId}")
            return ResponseEntity.badRequest()
                .body(mapOf(
                    "message" to "content 또는 files 중 하나 이상은 필수입니다",
                    "code" to "VALIDATION_FAILED"
                ))
        }
        
        logger.info("Upload request from user: ${principal.userId}, files: ${validFiles.size}, hasContent: ${!content.isNullOrBlank()}")

        // UploadRequest 객체 생성
        val request = UploadRequest(
            title = title,
            content = content
        )

        // 업로드 서비스 호출
        val response = uploadService.uploadFiles(
            request = request,
            files = if (validFiles.isEmpty()) null else validFiles,
            userId = principal.userId,
            institutionId = principal.institutionId!!
        )

        logger.info("File upload successful: ${response.id}")

        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    /**
     * 내 업로드 목록을 조회합니다.
     * 
     * @param authentication 인증 정보
     * @return 업로드 목록
     */
    @GetMapping
    @Operation(
        summary = "내 업로드 목록 조회",
        description = "현재 로그인한 사용자가 업로드한 파일 목록을 조회합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "목록 조회 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    fun getMyUploads(authentication: Authentication): ResponseEntity<*> {
        // 안전한 캐스팅: principal이 CustomUserPrincipal 타입이 아닌 경우 null 반환
        val principal = authentication.principal as? CustomUserPrincipal
            ?: run {
                logger.error("Invalid principal type: ${authentication.principal?.javaClass?.name}")
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(mapOf(
                        "message" to "인증 정보가 올바르지 않습니다",
                        "code" to "INVALID_AUTHENTICATION"
                    ))
            }

        val uploads = uploadService.getUserUploads(principal.userId)

        return ResponseEntity.ok(uploads)
    }

    /**
     * 특정 업로드의 상세 정보를 조회합니다.
     * 
     * @param uploadId 업로드 ID
     * @param authentication 인증 정보
     * @return 업로드 상세 정보
     */
    @GetMapping("/{uploadId}")
    @Operation(
        summary = "업로드 상세 조회",
        description = "특정 업로드의 상세 정보를 조회합니다. 자신이 업로드한 파일만 조회 가능합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "상세 조회 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "권한 부족 (다른 사용자의 업로드)"),
            ApiResponse(responseCode = "404", description = "업로드를 찾을 수 없음")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    fun getUploadById(
        @PathVariable uploadId: Long,
        authentication: Authentication
    ): ResponseEntity<*> {
        // 안전한 캐스팅: principal이 CustomUserPrincipal 타입이 아닌 경우 null 반환
        val principal = authentication.principal as? CustomUserPrincipal
            ?: run {
                logger.error("Invalid principal type: ${authentication.principal?.javaClass?.name}")
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(mapOf(
                        "message" to "인증 정보가 올바르지 않습니다",
                        "code" to "INVALID_AUTHENTICATION"
                    ))
            }

        val upload = uploadService.getUploadById(uploadId, principal.userId)

        return ResponseEntity.ok(upload)
    }
}

