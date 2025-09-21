package com.onsae.api.common.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/test")
@Tag(name = "테스트", description = "API 테스트를 위한 엔드포인트")
class TestController {

    @GetMapping("/health")
    @Operation(
        summary = "헬스 체크",
        description = "API 서버의 상태를 확인합니다."
    )
    @ApiResponse(responseCode = "200", description = "정상 상태")
    @SecurityRequirements
    fun healthCheck(): Map<String, String> {
        return mapOf(
            "status" to "OK",
            "message" to "온새 API 서버가 정상적으로 동작 중입니다."
        )
    }

    @GetMapping("/version")
    @Operation(
        summary = "버전 정보",
        description = "API 서버의 버전 정보를 반환합니다."
    )
    @ApiResponse(responseCode = "200", description = "버전 정보")
    @SecurityRequirements
    fun getVersion(): Map<String, String> {
        return mapOf(
            "version" to "1.0.0",
            "buildTime" to "2024-09-21",
            "environment" to "development"
        )
    }
}