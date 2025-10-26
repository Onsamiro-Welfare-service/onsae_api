package com.onsae.api.global.exception

import com.onsae.api.auth.exception.*
import com.onsae.api.common.exception.BusinessException
import com.onsae.api.dashboard.exception.*
import com.onsae.api.file.exception.*
import com.onsae.api.institution.exception.*
import com.onsae.api.notification.exception.*
import com.onsae.api.survey.exception.*
import com.onsae.api.user.exception.*
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest

private val logger = KotlinLogging.logger {}

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(Exception::class)
    fun handleGlobalException(
        ex: Exception,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.error(ex) { "Unexpected error occurred" }

        val errorResponse = ErrorResponse(
            message = "Internal server error",
            code = "INTERNAL_SERVER_ERROR",
            timestamp = System.currentTimeMillis()
        )

        return ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(
        ex: BusinessException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn { "Business exception: ${ex.message}" }

        val errorResponse = ErrorResponse(
            message = ex.message ?: "Business error occurred",
            code = ex.code,
            timestamp = System.currentTimeMillis()
        )

        return ResponseEntity(errorResponse, ex.status)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(
        ex: MethodArgumentNotValidException,
        request: WebRequest
    ): ResponseEntity<ValidationErrorResponse> {
        logger.warn { "Validation error: ${ex.message}" }

        val errors = ex.bindingResult.allErrors.map { error ->
            when (error) {
                is FieldError -> FieldErrorDetail(
                    field = error.field,
                    rejectedValue = error.rejectedValue?.toString(),
                    message = error.defaultMessage ?: "Validation failed"
                )
                else -> FieldErrorDetail(
                    field = "unknown",
                    rejectedValue = null,
                    message = error.defaultMessage ?: "Validation failed"
                )
            }
        }

        val errorResponse = ValidationErrorResponse(
            message = "Validation failed",
            code = "VALIDATION_FAILED",
            timestamp = System.currentTimeMillis(),
            errors = errors
        )

        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    // Auth domain exceptions
    @ExceptionHandler(
        InvalidCredentialsException::class,
        InvalidTokenException::class,
        TokenExpiredException::class
    )
    fun handleAuthException(
        ex: BusinessException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> = handleBusinessException(ex, request)

    // User domain exceptions
    @ExceptionHandler(
        UserNotFoundException::class,
        AdminNotFoundException::class,
        UserGroupNotFoundException::class,
        AdminApprovalPendingException::class,
        AdminApprovalRejectedException::class
    )
    fun handleUserException(
        ex: BusinessException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> = handleBusinessException(ex, request)

    // Institution domain exceptions
    @ExceptionHandler(
        InstitutionNotFoundException::class,
        InstitutionAccessDeniedException::class
    )
    fun handleInstitutionException(
        ex: BusinessException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> = handleBusinessException(ex, request)

    // Survey domain exceptions
    @ExceptionHandler(
        QuestionNotFoundException::class,
        CategoryNotFoundException::class,
        QuestionAssignmentNotFoundException::class,
        TemplateNotFoundException::class,
        InvalidResponseDataException::class,
        ResponseNotFoundException::class
    )
    fun handleSurveyException(
        ex: BusinessException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> = handleBusinessException(ex, request)

    // File domain exceptions
    @ExceptionHandler(
        FileNotFoundException::class,
        FileUploadException::class,
        InvalidFileTypeException::class,
        FileSizeExceededException::class,
        UploadNotFoundException::class
    )
    fun handleFileException(
        ex: BusinessException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> = handleBusinessException(ex, request)

    // Notification domain exceptions
    @ExceptionHandler(
        NotificationSendException::class,
        InvalidFcmTokenException::class
    )
    fun handleNotificationException(
        ex: BusinessException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> = handleBusinessException(ex, request)

    // Dashboard domain exceptions
    @ExceptionHandler(
        StatisticsCalculationException::class,
        ReportGenerationException::class,
        InvalidDateRangeException::class
    )
    fun handleDashboardException(
        ex: BusinessException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> = handleBusinessException(ex, request)
}

data class ErrorResponse(
    val message: String,
    val code: String,
    val timestamp: Long
)

data class ValidationErrorResponse(
    val message: String,
    val code: String,
    val timestamp: Long,
    val errors: List<FieldErrorDetail>
)

data class FieldErrorDetail(
    val field: String,
    val rejectedValue: String?,
    val message: String
)