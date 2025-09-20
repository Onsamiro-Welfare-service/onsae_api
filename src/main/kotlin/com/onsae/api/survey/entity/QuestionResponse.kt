package com.onsae.api.survey.entity

import com.onsae.api.user.entity.User
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.net.InetAddress
import java.time.LocalDateTime

@Entity
@Table(name = "question_responses")
class QuestionResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id")
    var assignment: QuestionAssignment? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    var question: Question? = null

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "response_data", nullable = false, columnDefinition = "jsonb")
    var responseData: Map<String, Any> = emptyMap()

    @Column(name = "response_text", columnDefinition = "TEXT")
    var responseText: String? = null

    @Column(name = "other_response", columnDefinition = "TEXT")
    var otherResponse: String? = null

    @Column(name = "response_time_seconds")
    var responseTimeSeconds: Int? = null

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "device_info", columnDefinition = "jsonb")
    var deviceInfo: Map<String, Any>? = null

    @Column(name = "submitted_at", nullable = false, updatable = false)
    var submittedAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "ip_address")
    var ipAddress: InetAddress? = null

    @Column(name = "user_agent", columnDefinition = "TEXT")
    var userAgent: String? = null
}