package com.onsae.api.file.service

import com.onsae.api.admin.repository.AdminRepository
import com.onsae.api.auth.exception.InvalidCredentialsException
import com.onsae.api.file.dto.AdminResponseRequest
import com.onsae.api.file.dto.UploadFileResponse
import com.onsae.api.file.dto.UploadListResponse
import com.onsae.api.file.dto.UploadRequest
import com.onsae.api.file.dto.UploadResponse
import com.onsae.api.file.entity.FileType
import com.onsae.api.file.entity.Upload
import com.onsae.api.file.entity.UploadFile
import com.onsae.api.file.exception.UploadNotFoundException
import com.onsae.api.file.repository.UploadFileRepository
import com.onsae.api.file.repository.UploadRepository
import com.onsae.api.institution.repository.InstitutionRepository
import com.onsae.api.user.repository.UserRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

/**
 * 업로드 서비스
 * 
 * 업로드 관련 비즈니스 로직을 처리하는 서비스입니다.
 * 
 * 주요 기능:
 * - 파일 업로드: 사용자가 파일을 업로드하고 Upload 엔티티 생성
 * - 업로드 조회: 업로드 목록 및 상세 정보 조회
 * - 관리자 응답: 관리자가 업로드에 대해 응답 처리
 * - 권한 검증: 기관별 접근 권한 검증
 */
@Service
@Transactional
class UploadService(
    private val uploadRepository: UploadRepository,
    private val uploadFileRepository: UploadFileRepository,
    private val institutionRepository: InstitutionRepository,
    private val userRepository: UserRepository,
    private val adminRepository: AdminRepository,
    private val fileStorageService: FileStorageService
) {
    private val logger = KotlinLogging.logger {}

    /**
     * 파일 또는 텍스트를 업로드합니다.
     * 
     * content와 files 중 하나 이상은 필수입니다.
     * 
     * @param request 업로드 요청 정보 (제목, 내용)
     * @param files 업로드할 파일들 (선택사항)
     * @param userId 업로드하는 사용자 ID
     * @param institutionId 기관 ID
     * @return 업로드 정보 응답
     */
    fun uploadFiles(
        request: UploadRequest,
        files: List<MultipartFile>?,
        userId: Long,
        institutionId: Long
    ): UploadResponse {
        val fileList = files?.filter { !it.isEmpty } ?: emptyList()
        logger.info("Upload request from user: $userId, institution: $institutionId, files: ${fileList.size}, hasContent: ${!request.content.isNullOrBlank()}")

        // 1. 기관 확인
        val institution = institutionRepository.findById(institutionId)
            .orElseThrow { InvalidCredentialsException("존재하지 않는 기관입니다") }

        // 2. 사용자 확인
        val user = userRepository.findById(userId)
            .orElseThrow { InvalidCredentialsException("존재하지 않는 사용자입니다") }

        // 3. content와 files 중 하나 이상은 필수
        val hasContent = !request.content.isNullOrBlank()
        val hasFiles = fileList.isNotEmpty()
        
        if (!hasContent && !hasFiles) {
            throw IllegalArgumentException("content 또는 files 중 하나 이상은 필수입니다")
        }

        // 4. Upload 엔티티 생성
        val upload = Upload().apply {
            this.institution = institution
            this.user = user
            this.title = request.title
            this.content = request.content
            this.adminRead = false
        }

        // 5. 파일이 있는 경우에만 파일 저장 및 UploadFile 엔티티 생성
        val uploadFiles = if (hasFiles) {
            fileList.mapIndexed { index, file ->
                // 파일 타입 자동 감지 (간단한 방식 - 확장자 기반)
                val fileType = detectFileType(file.originalFilename ?: "")

                // 파일 저장
                val fileInfo = fileStorageService.saveFile(file, fileType)

                // UploadFile 엔티티 생성
                UploadFile().apply {
                    this.upload = upload
                    this.fileType = fileType
                    this.fileName = fileInfo.fileName
                    this.originalName = fileInfo.originalName
                    this.filePath = fileInfo.filePath
                    this.fileSize = fileInfo.fileSize
                    this.mimeType = fileInfo.mimeType
                    this.uploadOrder = index + 1
                }
            }
        } else {
            emptyList()
        }

        // 6. Upload와 UploadFile 저장 (Cascade로 자동 저장됨)
        if (uploadFiles.isNotEmpty()) {
            upload.files.addAll(uploadFiles)
        }
        val savedUpload = uploadRepository.save(upload)

        logger.info("Upload created successfully: ${savedUpload.id}")

        return toUploadResponse(savedUpload)
    }

    /**
     * 사용자의 업로드 목록을 조회합니다.
     * 
     * @param userId 사용자 ID
     * @return 업로드 목록
     */
    @Transactional(readOnly = true)
    fun getUserUploads(userId: Long): List<UploadListResponse> {
        val uploads = uploadRepository.findByUserId(userId)
        
        // N+1 쿼리 문제 방지: 모든 업로드의 파일을 한 번에 배치 조회
        val uploadIds = uploads.mapNotNull { it.id }
        val filesByUploadId = if (uploadIds.isNotEmpty()) {
            uploadFileRepository.findByUploadIdInOrderByUploadOrder(uploadIds)
                .groupBy { it.upload.id!! }
        } else {
            emptyMap()
        }
        
        return uploads.map { upload ->
            val files = filesByUploadId[upload.id] ?: emptyList()
            toUploadListResponse(upload, files)
        }
    }

    /**
     * 특정 업로드의 상세 정보를 조회합니다.
     * 
     * @param uploadId 업로드 ID
     * @param userId 조회하는 사용자 ID (권한 검증용)
     * @return 업로드 상세 정보
     */
    @Transactional(readOnly = true)
    fun getUploadById(uploadId: Long, userId: Long): UploadResponse {
        val upload = uploadRepository.findById(uploadId)
            .orElseThrow { UploadNotFoundException("업로드를 찾을 수 없습니다") }

        // 권한 검증: 자신의 업로드만 조회 가능
        if (upload.user?.id != userId) {
            throw InvalidCredentialsException("해당 업로드에 대한 접근 권한이 없습니다")
        }

        return toUploadResponse(upload)
    }

    /**
     * 기관의 모든 업로드 목록을 조회합니다 (관리자용).
     * 
     * @param institutionId 기관 ID
     * @param limit 조회할 최대 개수 (null이면 전체 조회)
     * @param offset 건너뛸 개수 (null이면 0부터 시작)
     * @return 업로드 목록
     */
    @Transactional(readOnly = true)
    fun getAllUploadsByInstitution(
        institutionId: Long,
        limit: Int? = null,
        offset: Int? = null
    ): List<UploadListResponse> {
        val uploads = uploadRepository.findByInstitutionId(institutionId)
        
        // 페이징 처리: limit과 offset이 있으면 적용, 없으면 전체 반환
        val paginatedUploads = when {
            offset != null && limit != null -> {
                // offset과 limit 모두 있는 경우
                uploads.drop(offset).take(limit)
            }
            offset != null -> {
                // offset만 있는 경우 (limit 없으면 끝까지)
                uploads.drop(offset)
            }
            limit != null -> {
                // limit만 있는 경우 (offset 없으면 처음부터)
                uploads.take(limit)
            }
            else -> {
                // 둘 다 없는 경우 전체 반환
                uploads
            }
        }
        
        // N+1 쿼리 문제 방지: 모든 업로드의 파일을 한 번에 배치 조회
        // 페이징된 업로드의 ID만 사용하여 파일 조회
        val uploadIds = paginatedUploads.mapNotNull { it.id }
        val filesByUploadId = if (uploadIds.isNotEmpty()) {
            uploadFileRepository.findByUploadIdInOrderByUploadOrder(uploadIds)
                .groupBy { it.upload.id!! }
        } else {
            emptyMap()
        }
        
        return paginatedUploads.map { upload ->
            val files = filesByUploadId[upload.id] ?: emptyList()
            toUploadListResponse(upload, files)
        }
    }

    /**
     * 특정 업로드의 상세 정보를 조회합니다 (관리자용).
     * 같은 기관의 업로드만 조회 가능합니다.
     * 
     * @param uploadId 업로드 ID
     * @param institutionId 기관 ID
     * @return 업로드 상세 정보
     */
    @Transactional(readOnly = true)
    fun getUploadByIdForAdmin(uploadId: Long, institutionId: Long): UploadResponse {
        val upload = uploadRepository.findById(uploadId)
            .orElseThrow { UploadNotFoundException("업로드를 찾을 수 없습니다") }

        // 권한 검증: 같은 기관의 업로드만 조회 가능
        if (upload.institution.id != institutionId) {
            throw InvalidCredentialsException("해당 업로드에 대한 접근 권한이 없습니다")
        }

        return toUploadResponse(upload)
    }

    /**
     * 관리자가 업로드에 대해 응답합니다.
     * 
     * @param uploadId 업로드 ID
     * @param request 관리자 응답 요청
     * @param adminId 관리자 ID
     * @param institutionId 기관 ID
     * @return 업데이트된 업로드 정보
     */
    fun respondToUpload(
        uploadId: Long,
        request: AdminResponseRequest,
        adminId: Long,
        institutionId: Long
    ): UploadResponse {
        logger.info("Admin response to upload: $uploadId by admin: $adminId")

        // 1. 업로드 조회
        val upload = uploadRepository.findById(uploadId)
            .orElseThrow { UploadNotFoundException("업로드를 찾을 수 없습니다") }

        // 2. 권한 검증: 같은 기관의 업로드만 응답 가능
        if (upload.institution.id != institutionId) {
            throw InvalidCredentialsException("해당 업로드에 대한 접근 권한이 없습니다")
        }

        // 3. 관리자 조회
        val admin = adminRepository.findById(adminId)
            .orElseThrow { InvalidCredentialsException("존재하지 않는 관리자입니다") }

        // 4. 응답 정보 업데이트
        upload.adminRead = true
        upload.adminResponse = request.response
        upload.adminResponseDate = LocalDateTime.now()
        upload.admin = admin

        val savedUpload = uploadRepository.save(upload)
        logger.info("Admin response saved successfully")

        return toUploadResponse(savedUpload)
    }

    /**
     * 파일 타입을 자동 감지합니다 (확장자 기반).
     * 
     * @param filename 파일명
     * @return 파일 타입
     */
    private fun detectFileType(filename: String): FileType {
        val extension = filename.substringAfterLast('.', "").lowercase()
        return when (extension) {
            "jpg", "jpeg", "png", "gif", "webp" -> FileType.IMAGE
            "mp3", "wav", "m4a", "aac" -> FileType.AUDIO
            "mp4", "avi", "mov", "mkv" -> FileType.VIDEO
            "pdf", "doc", "docx", "xls", "xlsx" -> FileType.DOCUMENT
            "txt", "md" -> FileType.TEXT
            else -> FileType.DOCUMENT // 기본값
        }
    }

    /**
     * Upload 엔티티를 UploadResponse DTO로 변환합니다.
     * 
     * @param upload Upload 엔티티
     * @return UploadResponse DTO
     */
    private fun toUploadResponse(upload: Upload): UploadResponse {
        // 파일 목록을 로드하기 위해 엔티티를 초기화
        val files = uploadFileRepository.findByUploadIdOrderByUploadOrder(upload.id!!)

        return UploadResponse(
            id = upload.id!!,
            title = upload.title,
            content = upload.content,
            userId = upload.user?.id,
            userName = upload.user?.name,
            institutionId = upload.institution.id!!,
            institutionName = upload.institution.name,
            adminRead = upload.adminRead,
            adminResponse = upload.adminResponse,
            adminResponseDate = upload.adminResponseDate,
            adminId = upload.admin?.id,
            adminName = upload.admin?.name,
            files = files.map { toUploadFileResponse(it) },
            createdAt = upload.createdAt,
            updatedAt = upload.updatedAt
        )
    }

    /**
     * Upload 엔티티를 UploadListResponse DTO로 변환합니다.
     * 
     * @param upload Upload 엔티티
     * @param files 미리 로드된 파일 목록 (N+1 쿼리 방지를 위해 전달)
     * @return UploadListResponse DTO
     */
    private fun toUploadListResponse(upload: Upload, files: List<UploadFile> = emptyList()): UploadListResponse {
        // 파일이 전달되지 않은 경우에만 개별 조회 (단일 업로드 조회 시)
        val uploadFiles = if (files.isEmpty() && upload.id != null) {
            uploadFileRepository.findByUploadIdOrderByUploadOrder(upload.id!!)
        } else {
            files
        }
        
        val fileCount = uploadFiles.size
        val firstFileType = uploadFiles.firstOrNull()?.fileType

        // 내용 미리보기 (최대 100자)
        val content = upload.content
        val contentPreview = content?.take(100)?.let {
            if (content.length > 100) "$it..." else it
        }

        return UploadListResponse(
            id = upload.id!!,
            title = upload.title,
            contentPreview = contentPreview,
            userId = upload.user?.id,
            userName = upload.user?.name,
            institutionId = upload.institution.id!!,
            institutionName = upload.institution.name,
            adminRead = upload.adminRead,
            adminResponseDate = upload.adminResponseDate,
            fileCount = fileCount,
            firstFileType = firstFileType,
            createdAt = upload.createdAt
        )
    }

    /**
     * UploadFile 엔티티를 UploadFileResponse DTO로 변환합니다.
     * 
     * @param uploadFile UploadFile 엔티티
     * @return UploadFileResponse DTO
     */
    private fun toUploadFileResponse(uploadFile: UploadFile): UploadFileResponse {
        return UploadFileResponse(
            id = uploadFile.id!!,
            fileType = uploadFile.fileType,
            fileName = uploadFile.fileName,
            originalName = uploadFile.originalName,
            filePath = uploadFile.filePath,
            fileSize = uploadFile.fileSize,
            mimeType = uploadFile.mimeType,
            durationSeconds = uploadFile.durationSeconds,
            imageWidth = uploadFile.imageWidth,
            imageHeight = uploadFile.imageHeight,
            thumbnailPath = uploadFile.thumbnailPath,
            uploadOrder = uploadFile.uploadOrder,
            createdAt = uploadFile.createdAt
        )
    }
}

