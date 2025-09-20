package com.onsae.api.user.entity

import com.onsae.api.common.entity.BaseEntity
import com.onsae.api.institution.entity.Institution
import com.onsae.api.survey.entity.Category
import com.onsae.api.survey.entity.Question
import com.onsae.api.survey.entity.QuestionAssignment
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "admins",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["institution_id", "email"])
    ]
)
class Admin : BaseEntity() {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", nullable = false)
    var institution: Institution = Institution()

    @Column(name = "email", nullable = false, length = 100)
    var email: String = ""

    @Column(name = "password", nullable = false, length = 255)
    var password: String = ""

    @Column(name = "name", nullable = false, length = 50)
    var name: String = ""

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20)
    var role: AdminRole = AdminRole.STAFF

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    var status: AccountStatus = AccountStatus.PENDING

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    var approvedBy: Admin? = null

    @Column(name = "approved_at")
    var approvedAt: LocalDateTime? = null

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    var rejectionReason: String? = null

    @Column(name = "phone", length = 20)
    var phone: String? = null

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true

    @Column(name = "last_login")
    var lastLogin: LocalDateTime? = null

    @OneToMany(mappedBy = "createdBy", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var createdCategories: MutableList<Category> = mutableListOf()

    @OneToMany(mappedBy = "createdBy", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var createdQuestions: MutableList<Question> = mutableListOf()

    @OneToMany(mappedBy = "assignedBy", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var questionAssignments: MutableList<QuestionAssignment> = mutableListOf()
}