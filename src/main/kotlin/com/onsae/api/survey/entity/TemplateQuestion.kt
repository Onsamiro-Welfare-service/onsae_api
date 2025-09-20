package com.onsae.api.survey.entity

import jakarta.persistence.*

@Entity
@Table(
    name = "template_questions",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["template_id", "question_id"])
    ]
)
class TemplateQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    var template: AssignmentTemplate = AssignmentTemplate()

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    var question: Question? = null

    @Column(name = "priority", nullable = false)
    var priority: Int = 5

    @Column(name = "is_required", nullable = false)
    var isRequired: Boolean = false

    @Column(name = "display_order", nullable = false)
    var displayOrder: Int = 1
}