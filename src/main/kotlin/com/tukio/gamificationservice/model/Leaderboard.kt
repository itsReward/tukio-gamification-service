package com.tukio.gamificationservice.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "leaderboards")
data class Leaderboard(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    val name: String,

    @Column(nullable = false)
    val description: String,

    @Column(nullable = false)
    val timeFrame: TimeFrame,

    @Column(nullable = false)
    val category: LeaderboardCategory,

    @Column(nullable = true)
    val eventCategoryId: Long? = null,

    @Column(nullable = false)
    val isActive: Boolean = true,

    @Column(nullable = false)
    val lastUpdated: LocalDateTime = LocalDateTime.now()
)

enum class TimeFrame {
    WEEKLY,
    MONTHLY,
    SEMESTER,
    YEARLY,
    ALL_TIME
}

enum class LeaderboardCategory {
    OVERALL,                // Overall points
    EVENT_ATTENDANCE,       // Event attendance count
    BADGES_EARNED,          // Number of badges earned
    CATEGORY_SPECIFIC,      // Points for specific event category
    DIVERSE_PARTICIPATION   // Participation in diverse events
}