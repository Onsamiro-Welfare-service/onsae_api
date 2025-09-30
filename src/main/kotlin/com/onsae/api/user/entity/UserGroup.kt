package com.onsae.api.user.entity

import com.onsae.api.admin.entity.Admin
import com.onsae.api.common.entity.BaseEntity
import com.onsae.api.institution.entity.Institution
import com.onsae.api.survey.entity.QuestionAssignment
import jakarta.persistence.*

@Entity
@Table(
    name = "user_groups",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["institution_id", "name"])
    ]
)
class UserGroup : BaseEntity() {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", nullable = false)
    var institution: Institution = Institution()

    @Column(name = "name", nullable = false, length = 100)
    var name: String = ""

    @Column(name = "description", columnDefinition = "TEXT")
    var description: String? = null

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true

    @Column(name = "member_count", nullable = false)
    var memberCount: Int = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    var createdBy: Admin? = null

    @OneToMany(mappedBy = "group", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var members: MutableList<UserGroupMember> = mutableListOf()

    @OneToMany(mappedBy = "group", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var questionAssignments: MutableList<QuestionAssignment> = mutableListOf()
}