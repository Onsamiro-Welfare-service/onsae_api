package com.onsae.api.auth.exception

import com.onsae.api.common.exception.BusinessException
import org.springframework.http.HttpStatus

class InvalidCredentialsException(
    message: String = "Invalid credentials",
    code: String = "INVALID_CREDENTIALS"
) : BusinessException(message, code, HttpStatus.UNAUTHORIZED)

class InvalidTokenException(
    message: String = "Invalid token",
    code: String = "INVALID_TOKEN"
) : BusinessException(message, code, HttpStatus.UNAUTHORIZED)

class TokenExpiredException(
    message: String = "Token expired",
    code: String = "TOKEN_EXPIRED"
) : BusinessException(message, code, HttpStatus.UNAUTHORIZED)

class AccountNotActivatedException(
    message: String = "Account is not activated",
    code: String = "ACCOUNT_NOT_ACTIVATED"
) : BusinessException(message, code, HttpStatus.FORBIDDEN)

class AccountSuspendedException(
    message: String = "Account is suspended",
    code: String = "ACCOUNT_SUSPENDED"
) : BusinessException(message, code, HttpStatus.FORBIDDEN)