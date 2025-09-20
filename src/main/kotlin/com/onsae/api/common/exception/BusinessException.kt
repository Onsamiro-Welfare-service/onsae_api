package com.onsae.api.common.exception

import org.springframework.http.HttpStatus

open class BusinessException(
    message: String,
    val code: String,
    val status: HttpStatus = HttpStatus.BAD_REQUEST
) : RuntimeException(message)

class EntityNotFoundException(
    message: String = "Entity not found",
    code: String = "ENTITY_NOT_FOUND"
) : BusinessException(message, code, HttpStatus.NOT_FOUND)

class UnauthorizedException(
    message: String = "Unauthorized",
    code: String = "UNAUTHORIZED"
) : BusinessException(message, code, HttpStatus.UNAUTHORIZED)

class ForbiddenException(
    message: String = "Forbidden",
    code: String = "FORBIDDEN"
) : BusinessException(message, code, HttpStatus.FORBIDDEN)

class InvalidParameterException(
    message: String = "Invalid parameter",
    code: String = "INVALID_PARAMETER"
) : BusinessException(message, code, HttpStatus.BAD_REQUEST)

class DuplicateException(
    message: String = "Duplicate resource",
    code: String = "DUPLICATE_RESOURCE"
) : BusinessException(message, code, HttpStatus.CONFLICT)