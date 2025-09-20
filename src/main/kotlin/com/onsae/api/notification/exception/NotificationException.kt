package com.onsae.api.notification.exception

import com.onsae.api.common.exception.BusinessException
import org.springframework.http.HttpStatus

class NotificationSendException(
    message: String = "Notification send failed",
    code: String = "NOTIFICATION_SEND_FAILED"
) : BusinessException(message, code, HttpStatus.INTERNAL_SERVER_ERROR)

class InvalidFcmTokenException(
    message: String = "Invalid FCM token",
    code: String = "INVALID_FCM_TOKEN"
) : BusinessException(message, code, HttpStatus.BAD_REQUEST)

class NotificationTemplateNotFoundException(
    message: String = "Notification template not found",
    code: String = "NOTIFICATION_TEMPLATE_NOT_FOUND"
) : BusinessException(message, code, HttpStatus.NOT_FOUND)