package com.onsae.api.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(
    AppProperties::class,
    JwtProperties::class
)
class PropertiesConfig