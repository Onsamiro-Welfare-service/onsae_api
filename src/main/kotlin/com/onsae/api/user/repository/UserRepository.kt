package com.onsae.api.user.repository

import com.onsae.api.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByUsercode(usercode: String): Optional<User>
    fun existsByUsercode(usercode: String): Boolean
    fun findByInstitutionIdAndUsercode(institutionId: Long, usercode: String): Optional<User>
    fun existsByInstitutionIdAndUsercode(institutionId: Long, usercode: String): Boolean
    fun findByInstitutionId(institutionId: Long): List<User>
}