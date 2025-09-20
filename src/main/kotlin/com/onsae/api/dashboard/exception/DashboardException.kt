package com.onsae.api.dashboard.exception

import com.onsae.api.common.exception.BusinessException
import org.springframework.http.HttpStatus

class StatisticsCalculationException(
    message: String = "Statistics calculation failed",
    code: String = "STATISTICS_CALCULATION_FAILED"
) : BusinessException(message, code, HttpStatus.INTERNAL_SERVER_ERROR)

class ReportGenerationException(
    message: String = "Report generation failed",
    code: String = "REPORT_GENERATION_FAILED"
) : BusinessException(message, code, HttpStatus.INTERNAL_SERVER_ERROR)

class InvalidDateRangeException(
    message: String = "Invalid date range",
    code: String = "INVALID_DATE_RANGE"
) : BusinessException(message, code, HttpStatus.BAD_REQUEST)