package com.onsae.api.file.service

import com.onsae.api.config.AppProperties
import com.onsae.api.file.entity.FileType
import com.onsae.api.file.exception.FileStorageException
import com.onsae.api.file.exception.FileSizeExceededException
import com.onsae.api.file.exception.InvalidFileTypeException
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * 파일 저장 서비스
 * 
 * 업로드된 파일을 실제로 디스크에 저장하고 관리하는 역할을 합니다.
 * 
 * 주요 기능:
 * - 파일 저장: 업로드된 파일을 지정된 디렉토리에 저장
 * - 파일 타입 검증: 허용된 파일 타입만 업로드 가능
 * - 파일 크기 검증: 최대 파일 크기 제한
 * - 파일명 생성: 중복 방지를 위한 고유 파일명 생성
 */
@Service
class FileStorageService(
    private val appProperties: AppProperties
) {
    private val logger = KotlinLogging.logger {}

    // 업로드 디렉토리 경로 (application.yaml의 app.file-upload.path 값 사용)
    private val uploadPath: Path = Paths.get(appProperties.fileUpload.path)

    // 최대 파일 크기: 10MB (application.yaml에서 설정 가능)
    private val maxFileSize: Long = 10 * 1024 * 1024 // 10MB

    // 허용된 파일 확장자
    private val allowedExtensions = mapOf(
        FileType.IMAGE to setOf("jpg", "jpeg", "png", "gif", "webp"),
        FileType.AUDIO to setOf("mp3", "wav", "m4a", "aac"),
        FileType.VIDEO to setOf("mp4", "avi", "mov", "mkv"),
        FileType.DOCUMENT to setOf("pdf", "doc", "docx", "xls", "xlsx"),
        FileType.TEXT to setOf("txt", "md")
    )

    init {
        // 서비스가 시작될 때 업로드 디렉토리가 없으면 생성
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath)
            logger.info("Created upload directory: $uploadPath")
        }
    }

    /**
     * 파일을 저장하고 저장된 파일 정보를 반환합니다.
     * 
     * @param file 업로드된 파일
     * @param fileType 파일 타입 (IMAGE, AUDIO, VIDEO, DOCUMENT, TEXT)
     * @return 저장된 파일의 경로와 파일명 정보를 담은 FileInfo 객체
     */
    fun saveFile(file: MultipartFile, fileType: FileType): FileInfo {
        logger.info("Saving file: ${file.originalFilename}, type: $fileType, size: ${file.size}")

        // 1. 파일 타입 검증
        validateFileType(file, fileType)

        // 2. 파일 크기 검증
        validateFileSize(file.size)

        // 3. 파일명 생성 (중복 방지를 위해 UUID 사용)
        val originalFilename = file.originalFilename ?: "unknown"
        val extension = getFileExtension(originalFilename)
        val fileName = generateFileName(extension)

        // 4. 파일 저장 (업로드 경로에 직접 저장)
        val targetPath = uploadPath.resolve(fileName)
        try {
            // use 블록을 사용하여 스트림이 항상 닫히도록 보장
            // Kotlin의 use 함수는 finally 블록에서 자동으로 close()를 호출합니다
            file.inputStream.use { inputStream ->
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING)
            }
            logger.info("File saved successfully: $targetPath")
        } catch (e: Exception) {
            logger.error(e) { "Failed to save file: $originalFilename" }
            // 원래 예외를 cause로 포함하여 예외 체인 보존
            throw FileStorageException("파일 저장에 실패했습니다: ${e.message}", cause = e)
        }

        return FileInfo(
            fileName = fileName,
            originalName = originalFilename,
            filePath = fileName, // 파일명만 저장 (날짜별 디렉토리 구조 제거)
            fileSize = file.size,
            mimeType = file.contentType
        )
    }

    /**
     * 파일 타입을 검증합니다.
     * @param file 업로드된 파일
     * @param expectedType 예상되는 파일 타입
     */
    private fun validateFileType(file: MultipartFile, expectedType: FileType) {
        val originalFilename = file.originalFilename ?: throw InvalidFileTypeException("파일명이 없습니다")
        val extension = getFileExtension(originalFilename).lowercase()

        val allowed = allowedExtensions[expectedType]
            ?: throw InvalidFileTypeException("지원하지 않는 파일 타입입니다: $expectedType")

        if (!allowed.contains(extension)) {
            throw InvalidFileTypeException("$expectedType 타입의 파일이 아닙니다. 허용된 확장자: ${allowed.joinToString()}")
        }
    }

    /**
     * 파일 크기를 검증합니다.
     * @param fileSize 파일 크기 (bytes)
     */
    private fun validateFileSize(fileSize: Long) {
        if (fileSize > maxFileSize) {
            throw FileSizeExceededException("파일 크기가 너무 큽니다. 최대 크기: ${maxFileSize / 1024 / 1024}MB")
        }
    }

    /**
     * 파일 확장자를 추출합니다.
     * @param filename 파일명
     * @return 확장자 (소문자)
     */
    private fun getFileExtension(filename: String): String {
        val lastDot = filename.lastIndexOf('.')
        return if (lastDot >= 0) {
            filename.substring(lastDot + 1)
        } else {
            throw InvalidFileTypeException("파일 확장자가 없습니다")
        }
    }

    /**
     * 고유한 파일명을 생성합니다.
     * UUID를 사용하여 중복을 방지합니다.
     * @param extension 파일 확장자
     * @return 생성된 파일명
     */
    private fun generateFileName(extension: String): String {
        val uuid = UUID.randomUUID().toString().replace("-", "")
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
        return "${timestamp}_${uuid}.${extension}"
    }

    /**
     * 파일 정보를 담는 데이터 클래스
     */
    data class FileInfo(
        val fileName: String,
        val originalName: String,
        val filePath: String, // 파일명 (예: "20241106104630_abc123.jpg")
        val fileSize: Long,
        val mimeType: String?
    )

    /**
     * 파일을 삭제합니다.
     * @param filePath 삭제할 파일의 상대 경로
     */
    fun deleteFile(filePath: String) {
        try {
            val fullPath = uploadPath.resolve(filePath)
            if (Files.exists(fullPath)) {
                Files.delete(fullPath)
                logger.info("File deleted: $fullPath")
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to delete file: $filePath" }
            // 원래 예외를 cause로 포함하여 예외 체인 보존
            throw FileStorageException("파일 삭제에 실패했습니다: ${e.message}", cause = e)
        }
    }

    /**
     * 파일의 전체 경로를 반환합니다.
     * @param filePath 상대 경로
     * @return 전체 경로 (File 객체)
     */
    fun getFile(filePath: String): File {
        val fullPath = uploadPath.resolve(filePath)
        if (!Files.exists(fullPath)) {
            throw com.onsae.api.file.exception.FileNotFoundException("파일을 찾을 수 없습니다: $filePath")
        }
        return fullPath.toFile()
    }
}

