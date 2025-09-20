package com.onsae.api.file.entity

import com.onsae.api.common.entity.BaseEntity
import com.onsae.api.institution.entity.Institution
import com.onsae.api.user.entity.User
import com.onsae.api.user.entity.Admin
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "uploads")
class Upload : BaseEntity() {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", nullable = false)
    var institution: Institution = Institution()

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null

    @Column(name = "title", length = 200)
    var title: String? = null

    @Column(name = "content", columnDefinition = "TEXT")
    var content: String? = null

    @Column(name = "location_name", length = 100)
    var locationName: String? = null

    @Column(name = "coordinates", columnDefinition = "POINT")
    var coordinates: String? = null

    @Column(name = "admin_read", nullable = false)
    var adminRead: Boolean = false

    @Column(name = "admin_response", columnDefinition = "TEXT")
    var adminResponse: String? = null

    @Column(name = "admin_response_date")
    var adminResponseDate: LocalDateTime? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    var admin: Admin? = null

    @OneToMany(mappedBy = "upload", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var files: MutableList<UploadFile> = mutableListOf()
}