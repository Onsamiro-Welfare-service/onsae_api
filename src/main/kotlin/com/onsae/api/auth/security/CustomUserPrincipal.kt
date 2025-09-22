package com.onsae.api.auth.security

import org.springframework.security.core.GrantedAuthority

data class CustomUserPrincipal(
    val userId: Long,
    val userType: String,
    val institutionId: Long?,
    val authorities: Collection<GrantedAuthority>
)