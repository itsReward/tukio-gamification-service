package com.tukio.gamificationservice.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "leaderboard_entries")
data class LeaderboardEntry(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "leaderboard_id", nullable = false)
    val leaderboard: Leaderboard,

    @Column(nullable = false)
    val userId: Long,

    @Column(nullable = false)
    val rank: Int,

    @Column(nullable = false)
    val score: Int,

    @Column(nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
)