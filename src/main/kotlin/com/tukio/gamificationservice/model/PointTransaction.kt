package com.tukio.gamificationservice.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "point_transactions")
data class PointTransaction(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val userId: Long,

    @Column(nullable = false)
    val points: Int,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val activityType: ActivityType,

    @Column(nullable = true)
    val eventId: Long? = null,

    @Column(nullable = true)
    val description: String? = null,

    @Column(nullable = false)
    val timestamp: LocalDateTime = LocalDateTime.now()
)

