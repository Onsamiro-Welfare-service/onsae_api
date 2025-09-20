package com.onsae.api.survey.exception

import com.onsae.api.common.exception.BusinessException
import org.springframework.http.HttpStatus

class QuestionNotFoundException(
    message: String = "Question not found",
    code: String = "QUESTION_NOT_FOUND"
) : BusinessException(message, code, HttpStatus.NOT_FOUND)

class CategoryNotFoundException(
    message: String = "Category not found",
    code: String = "CATEGORY_NOT_FOUND"
) : BusinessException(message, code, HttpStatus.NOT_FOUND)

class CategoryAlreadyExistsException(
    message: String = "Category already exists",
    code: String = "CATEGORY_ALREADY_EXISTS"
) : BusinessException(message, code, HttpStatus.CONFLICT)

class QuestionAssignmentNotFoundException(
    message: String = "Question assignment not found",
    code: String = "QUESTION_ASSIGNMENT_NOT_FOUND"
) : BusinessException(message, code, HttpStatus.NOT_FOUND)

class QuestionAlreadyAssignedException(
    message: String = "Question is already assigned",
    code: String = "QUESTION_ALREADY_ASSIGNED"
) : BusinessException(message, code, HttpStatus.CONFLICT)

class ResponseAlreadySubmittedException(
    message: String = "Response already submitted",
    code: String = "RESPONSE_ALREADY_SUBMITTED"
) : BusinessException(message, code, HttpStatus.CONFLICT)

class InvalidResponseDataException(
    message: String = "Invalid response data",
    code: String = "INVALID_RESPONSE_DATA"
) : BusinessException(message, code, HttpStatus.BAD_REQUEST)

class TemplateNotFoundException(
    message: String = "Template not found",
    code: String = "TEMPLATE_NOT_FOUND"
) : BusinessException(message, code, HttpStatus.NOT_FOUND)

class TemplateAlreadyExistsException(
    message: String = "Template already exists",
    code: String = "TEMPLATE_ALREADY_EXISTS"
) : BusinessException(message, code, HttpStatus.CONFLICT)