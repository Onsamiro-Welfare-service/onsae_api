package com.onsae.api.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.transaction.annotation.EnableTransactionManagement

@Configuration
@EnableJpaRepositories(basePackages = [
    "com.onsae.api.auth.repository",
    "com.onsae.api.user.repository",
    "com.onsae.api.institution.repository",
    "com.onsae.api.survey.repository",
    "com.onsae.api.file.repository",
    "com.onsae.api.notification.repository",
    "com.onsae.api.dashboard.repository"
])
@EnableJpaAuditing
@EnableTransactionManagement
class DatabaseConfig