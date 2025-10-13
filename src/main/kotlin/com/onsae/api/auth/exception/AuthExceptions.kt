package com.onsae.api.auth.exception

import com.onsae.api.common.exception.BusinessException
import org.springframework.http.HttpStatus

class InvalidCredentialsException(
    message: String = "잘못된 인증 정보입니다"
) : BusinessException(message, "AUTH_001", HttpStatus.UNAUTHORIZED)

class InvalidTokenException(
    message: String = "유효하지 않은 토큰입니다"
) : BusinessException(message, "AUTH_002", HttpStatus.UNAUTHORIZED)

class TokenExpiredException(
    message: String = "토큰이 만료되었습니다"
) : BusinessException(message, "AUTH_003", HttpStatus.UNAUTHORIZED)

class AccountNotActivatedException(
    message: String = "계정이 활성화되지 않았습니다"
) : BusinessException(message, "AUTH_004", HttpStatus.FORBIDDEN)

class AccountSuspendedException(
    message: String = "계정이 정지되었습니다"
) : BusinessException(message, "AUTH_005", HttpStatus.FORBIDDEN)

class AccountPendingException(
    message: String = "계정 승인이 대기 중입니다"
) : BusinessException(message, "AUTH_006", HttpStatus.FORBIDDEN)