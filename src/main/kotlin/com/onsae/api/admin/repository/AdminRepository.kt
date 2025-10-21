package com.onsae.api.admin.repository

import com.onsae.api.admin.entity.Admin
import com.onsae.api.common.entity.AccountStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface AdminRepository : JpaRepository<Admin, Long> {
    fun findByEmail(email: String): Optional<Admin>
    fun existsByEmail(email: String): Boolean
    fun findByInstitutionIdAndEmail(institutionId: Long, email: String): Optional<Admin>
    fun existsByInstitutionIdAndEmail(institutionId: Long, email: String): Boolean
    fun findByStatus(status: AccountStatus): List<Admin>
    fun findByInstitutionId(institutionId: Long): List<Admin>
    fun countByInstitutionId(institutionId: Long): Int
}