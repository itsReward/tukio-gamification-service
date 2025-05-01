package com.tukio.gamificationservice.dto

import com.tukio.gamificationservice.model.*
import java.time.LocalDateTime

data class UserPointsDTO(
    val userId: Long,
    val totalPoints: Int,
    val monthlyPoints: Int,
    val weeklyPoints: Int,
    val level: Int,
    val nextLevelPoints: Int, // points needed for next level
    val levelProgress: Double // percentage to next level
)

data class BadgeDTO(
    val id: Long,
    val name: String,
    val description: String,
    val imageUrl: String,
    val requiredPoints: Int,
    val category: BadgeCategory,
    val tier: BadgeTier
)

data class UserBadgeDTO(
    val id: Long,
    val userId: Long,
    val badge: BadgeDTO,
    val awardedAt: LocalDateTime,
    val eventId: Long?
)

data class PointTransactionDTO(
    val userId: Long,
    val points: Int,
    val activityType: ActivityType,
    val eventId: Long? = null,
    val description: String? = null
)

data class PointTransactionResponseDTO(
    val id: Long,
    val userId: Long,
    val points: Int,
    val activityType: ActivityType,
    val eventId: Long? = null,
    val description: String? = null,
    val timestamp: LocalDateTime,
    val newTotalPoints: Int,
    val newLevel: Int,
    val badgesEarned: List<BadgeDTO> = emptyList()
)

data class LeaderboardDTO(
    val id: Long,
    val name: String,
    val description: String,
    val timeFrame: TimeFrame,
    val category: LeaderboardCategory,
    val eventCategoryId: Long? = null,
    val isActive: Boolean,
    val lastUpdated: LocalDateTime
)

data class LeaderboardEntryDTO(
    val leaderboardId: Long,
    val leaderboardName: String,
    val userId: Long,
    val username: String, // Populated from User Service
    val firstName: String,
    val lastName: String,
    val profilePictureUrl: String?,
    val rank: Int,
    val score: Int
)

data class LeaderboardResultDTO(
    val leaderboard: LeaderboardDTO,
    val entries: List<LeaderboardEntryDTO>,
    val userRank: LeaderboardEntryDTO? // Current user's rank
)

data class UserGamificationProfileDTO(
    val userId: Long,
    val username: String, // Populated from User Service
    val firstName: String,
    val lastName: String,
    val profilePictureUrl: String?,
    val totalPoints: Int,
    val level: Int,
    val levelProgress: Double,
    val badges: List<UserBadgeDTO>,
    val recentTransactions: List<PointTransactionResponseDTO>,
    val leaderboardRanks: List<LeaderboardEntryDTO>
)

data class ActivityEventDTO(
    val userId: Long,
    val eventId: Long,
    val activityType: String,
    val rating: Int? = null,
    val timestamp: LocalDateTime = LocalDateTime.now()
)