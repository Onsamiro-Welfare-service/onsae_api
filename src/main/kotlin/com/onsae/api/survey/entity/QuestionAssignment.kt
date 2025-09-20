package com.onsae.api.survey.entity

import com.onsae.api.institution.entity.Institution
import com.onsae.api.user.entity.Admin
import com.onsae.api.user.entity.User
import com.onsae.api.user.entity.UserGroup
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "question_assignments")
class QuestionAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", nullable = false)
    var institution: Institution = Institution()

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    var question: Question? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    var group: UserGroup? = null

    @Column(name = "priority", nullable = false)
    var priority: Int = 5

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by")
    var assignedBy: Admin? = null

    @Column(name = "assigned_at", nullable = false, updatable = false)
    var assignedAt: LocalDateTime = LocalDateTime.now()

    @OneToMany(mappedBy = "assignment", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var responses: MutableList<QuestionResponse> = mutableListOf()
}