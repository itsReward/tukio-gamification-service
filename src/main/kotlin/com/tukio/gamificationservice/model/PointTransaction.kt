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

    @Column(nullable = false)
    val activityType: ActivityType,

    @Column(nullable = true)
    val eventId: Long? = null,

    @Column(nullable = true)
    val description: String? = null,

    @Column(nullable = false)
    val timestamp: LocalDateTime = LocalDateTime.now()
)

enum class ActivityType {
    EVENT_REGISTRATION,      // Registering for an event
    EVENT_ATTENDANCE,        // Attending an event
    EVENT_RATING,            // Rating an event
    EVENT_SHARING,           // Sharing an event
    BADGE_EARNED,            // Earning a badge
    LEVEL_UP,                // Leveling up
    CONSECUTIVE_ATTENDANCE,  // Attending events consecutively
    DIVERSE_CATEGORIES,      // Attending diverse event categories
    REFERRAL,                // Referring another user
    BONUS,                   // Bonus points (admin awarded)
    POINT_ADJUSTMENT         // Administrative adjustment
}