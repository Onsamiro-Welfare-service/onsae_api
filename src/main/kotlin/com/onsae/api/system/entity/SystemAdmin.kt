package com.onsae.api.system.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "system_admins")
class SystemAdmin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(name = "email", unique = true, nullable = false, length = 100)
    var email: String = ""

    @Column(name = "password", nullable = false, length = 255)
    var password: String = ""

    @Column(name = "name", nullable = false, length = 50)
    var name: String = ""

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true

    @Column(name = "last_login")
    var lastLogin: LocalDateTime? = null

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
}