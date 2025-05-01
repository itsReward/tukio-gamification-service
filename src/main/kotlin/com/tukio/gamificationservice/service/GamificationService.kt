package com.tukio.gamificationservice.service

import com.tukio.gamificationservice.dto.*
import com.tukio.gamificationservice.model.ActivityType

interface GamificationService {

    // User profile and points management
    fun getUserGamificationProfile(userId: Long): UserGamificationProfileDTO

    fun processActivityEvent(activityEvent: ActivityEventDTO): PointTransactionResponseDTO

    // Common activities tracking
    fun recordEventRegistration(userId: Long, eventId: Long): PointTransactionResponseDTO

    fun recordEventAttendance(userId: Long, eventId: Long): PointTransactionResponseDTO

    fun recordEventRating(userId: Long, eventId: Long, rating: Int): PointTransactionResponseDTO

    fun recordEventSharing(userId: Long, eventId: Long): PointTransactionResponseDTO

    // Leaderboard access
    fun getLeaderboards(): List<LeaderboardDTO>

    fun getLeaderboard(leaderboardId: Long, userId: Long?, limit: Int): LeaderboardResultDTO

    // Badge management
    fun getUserBadges(userId: Long): List<UserBadgeDTO>

    fun getAvailableBadges(): List<BadgeDTO>

    // Analytics and statistics
    fun getUserActivityStats(userId: Long): Map<String, Int>

    fun getMostActiveUsers(limit: Int): List<UserPointsDTO>

    fun getMostPopularActivities(): Map<ActivityType, Int>
}