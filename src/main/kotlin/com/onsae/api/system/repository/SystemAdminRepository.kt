package com.onsae.api.system.repository

import com.onsae.api.system.entity.SystemAdmin
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface SystemAdminRepository : JpaRepository<SystemAdmin, Long> {
    fun findByEmail(email: String): Optional<SystemAdmin>
    fun existsByEmail(email: String): Boolean
}