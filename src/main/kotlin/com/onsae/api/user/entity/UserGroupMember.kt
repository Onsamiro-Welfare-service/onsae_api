package com.onsae.api.user.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "user_group_members",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["group_id", "user_id"])
    ]
)
class UserGroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    var group: UserGroup = UserGroup()

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User = User()

    @Column(name = "joined_at", nullable = false, updatable = false)
    var joinedAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "added_by")
    var addedBy: Admin? = null
}