package com.onsae.api.file.exception

import com.onsae.api.common.exception.BusinessException
import org.springframework.http.HttpStatus

class FileNotFoundException(
    message: String = "File not found",
    code: String = "FILE_NOT_FOUND"
) : BusinessException(message, code, HttpStatus.NOT_FOUND)

class FileUploadException(
    message: String = "File upload failed",
    code: String = "FILE_UPLOAD_FAILED"
) : BusinessException(message, code, HttpStatus.INTERNAL_SERVER_ERROR)

class InvalidFileTypeException(
    message: String = "Invalid file type",
    code: String = "INVALID_FILE_TYPE"
) : BusinessException(message, code, HttpStatus.BAD_REQUEST)

class FileSizeExceededException(
    message: String = "File size exceeded",
    code: String = "FILE_SIZE_EXCEEDED"
) : BusinessException(message, code, HttpStatus.BAD_REQUEST)

class FileStorageException(
    message: String = "File storage error",
    code: String = "FILE_STORAGE_ERROR"
) : BusinessException(message, code, HttpStatus.INTERNAL_SERVER_ERROR)

class UploadNotFoundException(
    message: String = "Upload not found",
    code: String = "UPLOAD_NOT_FOUND"
) : BusinessException(message, code, HttpStatus.NOT_FOUND)

class FileProcessingException(
    message: String = "File processing failed",
    code: String = "FILE_PROCESSING_FAILED"
) : BusinessException(message, code, HttpStatus.INTERNAL_SERVER_ERROR)