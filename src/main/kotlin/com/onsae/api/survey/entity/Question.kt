package com.onsae.api.survey.entity

import com.onsae.api.common.entity.BaseEntity
import com.onsae.api.institution.entity.Institution
import com.onsae.api.user.entity.Admin
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Entity
@Table(name = "questions")
class Question : BaseEntity() {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", nullable = false)
    var institution: Institution = Institution()

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    var category: Category? = null

    @Column(name = "title", nullable = false, length = 200)
    var title: String = ""

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    var content: String = ""

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false)
    var questionType: QuestionType = QuestionType.TEXT

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "options", columnDefinition = "jsonb")
    var options: Map<String, Any>? = null

    @Column(name = "allow_other_option", nullable = false)
    var allowOtherOption: Boolean = false

    @Column(name = "other_option_label", length = 50)
    var otherOptionLabel: String = "기타"

    @Column(name = "other_option_placeholder", length = 100)
    var otherOptionPlaceholder: String? = null

    @Column(name = "is_required", nullable = false)
    var isRequired: Boolean = false

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    var createdBy: Admin? = null

    @OneToMany(mappedBy = "question", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var assignments: MutableList<QuestionAssignment> = mutableListOf()

    @OneToMany(mappedBy = "question", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var responses: MutableList<QuestionResponse> = mutableListOf()

    @OneToMany(mappedBy = "question", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var templateQuestions: MutableList<TemplateQuestion> = mutableListOf()
}