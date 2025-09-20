package com.onsae.api.user.exception

import com.onsae.api.common.exception.BusinessException
import org.springframework.http.HttpStatus

class UserNotFoundException(
    message: String = "User not found",
    code: String = "USER_NOT_FOUND"
) : BusinessException(message, code, HttpStatus.NOT_FOUND)

class UserAlreadyExistsException(
    message: String = "User already exists",
    code: String = "USER_ALREADY_EXISTS"
) : BusinessException(message, code, HttpStatus.CONFLICT)

class AdminNotFoundException(
    message: String = "Admin not found",
    code: String = "ADMIN_NOT_FOUND"
) : BusinessException(message, code, HttpStatus.NOT_FOUND)

class AdminAlreadyExistsException(
    message: String = "Admin already exists",
    code: String = "ADMIN_ALREADY_EXISTS"
) : BusinessException(message, code, HttpStatus.CONFLICT)

class AdminApprovalPendingException(
    message: String = "Admin approval is pending",
    code: String = "ADMIN_APPROVAL_PENDING"
) : BusinessException(message, code, HttpStatus.FORBIDDEN)

class AdminApprovalRejectedException(
    message: String = "Admin approval was rejected",
    code: String = "ADMIN_APPROVAL_REJECTED"
) : BusinessException(message, code, HttpStatus.FORBIDDEN)

class UserGroupNotFoundException(
    message: String = "User group not found",
    code: String = "USER_GROUP_NOT_FOUND"
) : BusinessException(message, code, HttpStatus.NOT_FOUND)

class UserGroupAlreadyExistsException(
    message: String = "User group already exists",
    code: String = "USER_GROUP_ALREADY_EXISTS"
) : BusinessException(message, code, HttpStatus.CONFLICT)

class UserAlreadyInGroupException(
    message: String = "User is already in the group",
    code: String = "USER_ALREADY_IN_GROUP"
) : BusinessException(message, code, HttpStatus.CONFLICT)