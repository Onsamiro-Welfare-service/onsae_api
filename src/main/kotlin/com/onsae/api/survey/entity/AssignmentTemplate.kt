package com.onsae.api.survey.entity

import com.onsae.api.common.entity.BaseEntity
import com.onsae.api.institution.entity.Institution
import com.onsae.api.admin.entity.Admin
import jakarta.persistence.*

@Entity
@Table(
    name = "assignment_templates",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["institution_id", "name"])
    ]
)
class AssignmentTemplate : BaseEntity() {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id")
    var institution: Institution? = null

    @Column(name = "name", nullable = false, length = 100)
    var name: String = ""

    @Column(name = "description", columnDefinition = "TEXT")
    var description: String? = null

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", length = 20)
    var visibility: TemplateVisibility = TemplateVisibility.PRIVATE

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    var targetType: TargetType = TargetType.USER

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true

    @Column(name = "tags")
    var tags: Array<String>? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    var createdBy: Admin? = null

    @OneToMany(mappedBy = "template", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var templateQuestions: MutableList<TemplateQuestion> = mutableListOf()
}