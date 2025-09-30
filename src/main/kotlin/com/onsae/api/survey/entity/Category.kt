package com.onsae.api.survey.entity

import com.onsae.api.common.entity.BaseEntity
import com.onsae.api.institution.entity.Institution
import com.onsae.api.admin.entity.Admin
import jakarta.persistence.*

@Entity
@Table(
    name = "categories",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["institution_id", "name"])
    ]
)
class Category : BaseEntity() {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", nullable = false)
    var institution: Institution = Institution()

    @Column(name = "name", nullable = false, length = 100)
    var name: String = ""

    @Column(name = "description", columnDefinition = "TEXT")
    var description: String? = null

    @Column(name = "image_path", length = 500)
    var imagePath: String? = null

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    var createdBy: Admin? = null

    @OneToMany(mappedBy = "category", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var questions: MutableList<Question> = mutableListOf()
}