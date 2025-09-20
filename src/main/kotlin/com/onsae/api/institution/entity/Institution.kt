package com.onsae.api.institution.entity

import com.onsae.api.common.entity.BaseEntity
import com.onsae.api.user.entity.Admin
import com.onsae.api.user.entity.User
import com.onsae.api.survey.entity.Category
import com.onsae.api.survey.entity.Question
import jakarta.persistence.*

@Entity
@Table(name = "institutions")
class Institution : BaseEntity() {

    @Column(name = "name", nullable = false, length = 200)
    var name: String = ""

    @Column(name = "business_number", unique = true, length = 50)
    var businessNumber: String? = null

    @Column(name = "registration_number", length = 50)
    var registrationNumber: String? = null

    @Column(name = "address", columnDefinition = "TEXT")
    var address: String? = null

    @Column(name = "phone", length = 20)
    var phone: String? = null

    @Column(name = "email", length = 100)
    var email: String? = null

    @Column(name = "website", length = 200)
    var website: String? = null

    @Column(name = "director_name", length = 100)
    var directorName: String? = null

    @Column(name = "contact_person", length = 100)
    var contactPerson: String? = null

    @Column(name = "contact_phone", length = 20)
    var contactPhone: String? = null

    @Column(name = "contact_email", length = 100)
    var contactEmail: String? = null

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true

    @Column(name = "timezone", length = 50)
    var timezone: String = "Asia/Seoul"

    @Column(name = "locale", length = 10)
    var locale: String = "ko_KR"

    @OneToMany(mappedBy = "institution", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var admins: MutableList<Admin> = mutableListOf()

    @OneToMany(mappedBy = "institution", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var users: MutableList<User> = mutableListOf()

    @OneToMany(mappedBy = "institution", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var categories: MutableList<Category> = mutableListOf()

    @OneToMany(mappedBy = "institution", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var questions: MutableList<Question> = mutableListOf()
}