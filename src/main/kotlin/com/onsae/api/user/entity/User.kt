package com.onsae.api.user.entity

import com.onsae.api.common.entity.BaseEntity
import com.onsae.api.institution.entity.Institution
import com.onsae.api.survey.entity.QuestionAssignment
import com.onsae.api.survey.entity.QuestionResponse
import com.onsae.api.file.entity.Upload
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(
    name = "users",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["institution_id", "usercode"])
    ]
)
class User : BaseEntity() {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", nullable = false)
    var institution: Institution = Institution()

    @Column(name = "usercode", nullable = false, length = 20)
    var usercode: String = ""

    @Column(name = "name", nullable = false, length = 50)
    var name: String = ""

    @Column(name = "phone", length = 20)
    var phone: String? = null

    @Column(name = "address", columnDefinition = "TEXT")
    var address: String? = null

    @Column(name = "birth_date")
    var birthDate: LocalDate? = null

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false)
    var severity: SeverityLevel = SeverityLevel.MILD

    @Column(name = "guardian_name", length = 50)
    var guardianName: String? = null

    @Column(name = "guardian_relationship", length = 20)
    var guardianRelationship: String? = null

    @Column(name = "guardian_phone", length = 20)
    var guardianPhone: String? = null

    @Column(name = "guardian_email", length = 100)
    var guardianEmail: String? = null

    @Column(name = "guardian_address", columnDefinition = "TEXT")
    var guardianAddress: String? = null

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "emergency_contacts", columnDefinition = "jsonb")
    var emergencyContacts: Map<String, Any>? = null

    @Column(name = "care_notes", columnDefinition = "TEXT")
    var careNotes: String? = null

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true

    @Column(name = "last_login")
    var lastLogin: LocalDateTime? = null

    @Column(name = "fcm_token", length = 500)
    var fcmToken: String? = null

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var groupMemberships: MutableList<UserGroupMember> = mutableListOf()

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var questionAssignments: MutableList<QuestionAssignment> = mutableListOf()

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var questionResponses: MutableList<QuestionResponse> = mutableListOf()

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var uploads: MutableList<Upload> = mutableListOf()
}