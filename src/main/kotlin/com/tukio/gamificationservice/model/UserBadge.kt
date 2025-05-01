package com.tukio.gamificationservice.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "user_badges")
data class UserBadge(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val userId: Long,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "badge_id", nullable = false)
    val badge: Badge,

    @Column(nullable = false)
    val awardedAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = true)
    val eventId: Long? = null
)