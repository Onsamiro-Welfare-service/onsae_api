package com.onsae.api.admin.repository

import com.onsae.api.admin.entity.Admin
import com.onsae.api.common.entity.AccountStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
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

    @Query("SELECT a FROM Admin a LEFT JOIN FETCH a.institution LEFT JOIN FETCH a.approvedBy WHERE a.status = :status")
    fun findByStatusWithRelations(status: AccountStatus): List<Admin>

    @Query("SELECT a FROM Admin a LEFT JOIN FETCH a.institution LEFT JOIN FETCH a.approvedBy")
    fun findAllWithRelations(): List<Admin>

    @Query("SELECT a.institution.id as institutionId, COUNT(a) as count FROM Admin a WHERE a.institution.id IN :institutionIds GROUP BY a.institution.id")
    fun countByInstitutionIds(institutionIds: List<Long>): List<AdminCountProjection>

    interface AdminCountProjection {
        fun getInstitutionId(): Long
        fun getCount(): Long
    }
}