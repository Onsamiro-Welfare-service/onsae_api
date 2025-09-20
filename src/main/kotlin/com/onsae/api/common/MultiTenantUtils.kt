package com.onsae.api.common

import com.onsae.api.common.exception.UnauthorizedException
import org.springframework.security.core.context.SecurityContextHolder

object MultiTenantUtils {

    fun getCurrentInstitutionId(): Long {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw UnauthorizedException("No authentication found")

        return when (val principal = authentication.principal) {
            is InstitutionAwarePrincipal -> principal.institutionId
            else -> throw UnauthorizedException("Invalid authentication principal")
        }
    }

    fun validateInstitutionAccess(institutionId: Long) {
        val currentInstitutionId = getCurrentInstitutionId()
        if (currentInstitutionId != institutionId) {
            throw UnauthorizedException("Access denied to institution $institutionId")
        }
    }
}

interface InstitutionAwarePrincipal {
    val institutionId: Long
}