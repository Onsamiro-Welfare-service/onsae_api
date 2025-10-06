package com.onsae.api.institution.repository

import com.onsae.api.institution.entity.Institution
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface InstitutionRepository : JpaRepository<Institution, Long> {
    fun findByName(name: String): Optional<Institution>
    fun existsByName(name: String): Boolean
    fun findByBusinessNumber(businessNumber: String): Optional<Institution>
    fun existsByBusinessNumber(businessNumber: String): Boolean
    fun findByIsActiveTrue(): List<Institution>
}