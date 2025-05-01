package com.tukio.gamificationservice.model

import jakarta.persistence.*

@Entity
@Table(name = "badges")
data class Badge(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    val name: String,

    @Column(nullable = false)
    val description: String,

    @Column(nullable = false)
    val imageUrl: String,

    @Column(nullable = false)
    val requiredPoints: Int,

    @Column(nullable = false)
    val category: BadgeCategory,

    @Column(nullable = false)
    val tier: BadgeTier
)

enum class BadgeCategory {
    ATTENDANCE,      // Attending events
    REGISTRATION,    // Registering for events
    RATING,          // Rating events
    DIVERSITY,       // Attending diverse types of events
    PARTICIPATION,   // Participating actively in events
    ORGANIZATION,    // Helping organize events
    SOCIAL,          // Social sharing and inviting others
    MILESTONE        // Milestone achievements
}

enum class BadgeTier {
    BRONZE,
    SILVER,
    GOLD,
    PLATINUM
}