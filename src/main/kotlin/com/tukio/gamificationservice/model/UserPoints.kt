package com.tukio.gamificationservice.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "user_points")
data class UserPoints(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val userId: Long,

    @Column(nullable = false)
    var totalPoints: Int = 0,

    @Column(nullable = false)
    var monthlyPoints: Int = 0,

    @Column(nullable = false)
    var weeklyPoints: Int = 0,

    @Column(nullable = false)
    var level: Int = 1,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)