package com.onsae.api.institution.exception

import com.onsae.api.common.exception.BusinessException
import org.springframework.http.HttpStatus

class InstitutionNotFoundException(
    message: String = "Institution not found",
    code: String = "INSTITUTION_NOT_FOUND"
) : BusinessException(message, code, HttpStatus.NOT_FOUND)

class InstitutionAlreadyExistsException(
    message: String = "Institution already exists",
    code: String = "INSTITUTION_ALREADY_EXISTS"
) : BusinessException(message, code, HttpStatus.CONFLICT)

class InstitutionNotActiveException(
    message: String = "Institution is not active",
    code: String = "INSTITUTION_NOT_ACTIVE"
) : BusinessException(message, code, HttpStatus.FORBIDDEN)

class InstitutionAccessDeniedException(
    message: String = "Access denied to institution",
    code: String = "INSTITUTION_ACCESS_DENIED"
) : BusinessException(message, code, HttpStatus.FORBIDDEN)