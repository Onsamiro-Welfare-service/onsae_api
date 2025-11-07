package com.onsae.api.file.controller

import com.onsae.api.file.exception.UploadNotFoundException
import com.onsae.api.file.repository.UploadFileRepository
import com.onsae.api.file.service.FileStorageService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag
import mu.KotlinLogging
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 파일 컨트롤러
 * 
 * 업로드된 파일을 조회하는 API를 제공합니다.
 * 
 * 주요 기능:
 * - 파일 조회: GET /api/files/{fileId} - upload_files 테이블의 파일 ID로 조회
 */
private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/api/files")
@Tag(name = "파일", description = "파일 조회 API")
class FileController(
    private val uploadFileRepository: UploadFileRepository,
    private val fileStorageService: FileStorageService
) {

    /**
     * 파일을 조회합니다.
     * 
     * @param fileId upload_files 테이블의 파일 ID
     * @return 파일 리소스
     */
    @GetMapping("/{fileId}")
    @Operation(
        summary = "파일 조회",
        description = "업로드된 파일을 조회합니다. upload_files 테이블의 파일 ID를 사용합니다. 경로는 /api/files/{fileId} 형식입니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "파일 조회 성공"),
            ApiResponse(responseCode = "404", description = "파일을 찾을 수 없음")
        ]
    )
    @SecurityRequirements
    fun getFile(@PathVariable fileId: Long): ResponseEntity<Resource> {
        logger.debug("File request: fileId=$fileId")

        // 1. upload_files 테이블에서 파일 정보 조회
        val uploadFile = uploadFileRepository.findById(fileId)
            .orElseThrow { UploadNotFoundException("파일을 찾을 수 없습니다: fileId=$fileId") }

        // 2. file_path를 사용해서 파일 서빙
        val filePath = uploadFile.filePath
        logger.debug("Serving file: filePath=$filePath")

        try {
            val file = fileStorageService.getFile(filePath)
            val resource: Resource = FileSystemResource(file)

            if (!resource.exists() || !resource.isReadable) {
                logger.warn("File not found or not readable: $filePath")
                return ResponseEntity.notFound().build()
            }

            // MIME 타입 결정 (DB에 저장된 mimeType 우선, 없으면 확장자 기반)
            val contentType = uploadFile.mimeType ?: determineContentType(filePath)

            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"${uploadFile.originalName ?: uploadFile.fileName}\"")
                .body(resource)
        } catch (e: com.onsae.api.file.exception.FileNotFoundException) {
            logger.warn("File not found: $filePath")
            return ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error(e) { "Error serving file: filePath=$filePath" }
            return ResponseEntity.internalServerError().build()
        }
    }

    /**
     * 파일 확장자에 따라 MIME 타입을 결정합니다.
     */
    private fun determineContentType(filePath: String): String {
        val extension = filePath.substringAfterLast('.', "").lowercase()
        return when (extension) {
            "png" -> "image/png"
            "jpg", "jpeg" -> "image/jpeg"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            "mp3" -> "audio/mpeg"
            "wav" -> "audio/wav"
            "m4a" -> "audio/mp4"
            "aac" -> "audio/aac"
            "mp4" -> "video/mp4"
            "avi" -> "video/x-msvideo"
            "mov" -> "video/quicktime"
            "mkv" -> "video/x-matroska"
            "pdf" -> "application/pdf"
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "xls" -> "application/vnd.ms-excel"
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            "txt" -> "text/plain"
            "md" -> "text/markdown"
            else -> "application/octet-stream"
        }
    }
}

