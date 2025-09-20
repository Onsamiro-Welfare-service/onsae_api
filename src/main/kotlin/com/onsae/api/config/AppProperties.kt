package com.onsae.api.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app")
data class AppProperties(
    val jwt: JwtProperties,
    val fileUpload: FileUploadProperties,
    val cors: CorsProperties,
    val timezone: String,
    val locale: String
)

data class FileUploadProperties(
    val path: String
)

data class CorsProperties(
    val allowedOrigins: String
)