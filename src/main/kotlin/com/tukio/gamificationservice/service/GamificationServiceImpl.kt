package com.tukio.gamificationservice.service

import com.tukio.gamificationservice.client.EventServiceClient
import com.tukio.gamificationservice.client.UserServiceClient
import com.tukio.gamificationservice.dto.*
import com.tukio.gamificationservice.model.ActivityType
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GamificationServiceImpl(
    private val pointService: PointService,
    private val badgeService: BadgeService,
    private val leaderboardService: LeaderboardService,
    private val userServiceClient: UserServiceClient,
    private val eventServiceClient: EventServiceClient
) : GamificationService {

    private val logger = LoggerFactory.getLogger(GamificationServiceImpl::class.java)

    override fun getUserGamificationProfile(userId: Long): UserGamificationProfileDTO {
        // Get user details
        val user = try {
            userServiceClient.getUserById(userId)
        } catch (e: Exception) {
            logger.error("Failed to fetch user details for user $userId: ${e.message}")
            null
        }

        // Get user points
        val userPoints = pointService.getUserPoints(userId)

        // Get user badges
        val badges = badgeService.getUserBadges(userId)

        // Get recent transactions
        val recentTransactions = pointService.getUserTransactions(
            userId,
            PageRequest.of(0, 10)
        ).content

        // Get user rankings in leaderboards
        val leaderboardRanks = leaderboardService.getUserRankings(userId)

        return UserGamificationProfileDTO(
            userId = userId,
            username = user?.username ?: "unknown_user",
            firstName = user?.firstName ?: "Unknown",
            lastName = user?.lastName ?: "User",
            profilePictureUrl = user?.profilePictureUrl,
            totalPoints = userPoints.totalPoints,
            level = userPoints.level,
            levelProgress = userPoints.levelProgress,
            badges = badges,
            recentTransactions = recentTransactions,
            leaderboardRanks = leaderboardRanks
        )
    }

    @Transactional
    override fun processActivityEvent(activityEvent: ActivityEventDTO): PointTransactionResponseDTO {
        return when (activityEvent.activityType.uppercase()) {
            "REGISTRATION" -> recordEventRegistration(activityEvent.userId, activityEvent.eventId)
            "ATTENDANCE" -> recordEventAttendance(activityEvent.userId, activityEvent.eventId)
            "RATING" -> {
                val rating = activityEvent.rating ?: 3 // Default to 3 if no rating provided
                recordEventRating(activityEvent.userId, activityEvent.eventId, rating)
            }
            "SHARING" -> recordEventSharing(activityEvent.userId, activityEvent.eventId)
            else -> {
                logger.warn("Unknown activity type: ${activityEvent.activityType}")
                // Create a generic activity transaction
                val points = 1 // Default to 1 point for unknown activities
                val transaction = PointTransactionDTO(
                    userId = activityEvent.userId,
                    points = points,
                    activityType = ActivityType.BONUS,
                    eventId = activityEvent.eventId,
                    description = "Unknown activity: ${activityEvent.activityType}"
                )
                pointService.addPoints(transaction)
            }
        }
    }

    @Transactional
    override fun recordEventRegistration(userId: Long, eventId: Long): PointTransactionResponseDTO {
        logger.info("Recording event registration for user $userId, event $eventId")

        // Fetch event details if needed for more complex logic
        val event = try {
            eventServiceClient.getEventById(eventId)
        } catch (e: Exception) {
            logger.error("Failed to fetch event details for event $eventId: ${e.message}")
            null
        }

        // Award points for registration (5 points)
        val transaction = PointTransactionDTO(
            userId = userId,
            points = 5,
            activityType = ActivityType.EVENT_REGISTRATION,
            eventId = eventId,
            description = "Registered for event: ${event?.title ?: "Unknown Event"}"
        )

        return pointService.addPoints(transaction)
    }

    @Transactional
    override fun recordEventAttendance(userId: Long, eventId: Long): PointTransactionResponseDTO {
        logger.info("Recording event attendance for user $userId, event $eventId")

        // Fetch event details if needed
        val event = try {
            eventServiceClient.getEventById(eventId)
        } catch (e: Exception) {
            logger.error("Failed to fetch event details for event $eventId: ${e.message}")
            null
        }

        // Award points for attendance (10 points)
        val transaction = PointTransactionDTO(
            userId = userId,
            points = 10,
            activityType = ActivityType.EVENT_ATTENDANCE,
            eventId = eventId,
            description = "Attended event: ${event?.title ?: "Unknown Event"}"
        )

        return pointService.addPoints(transaction)
    }

    @Transactional
    override fun recordEventRating(userId: Long, eventId: Long, rating: Int): PointTransactionResponseDTO {
        logger.info("Recording event rating for user $userId, event $eventId, rating $rating")

        // Fetch event details if needed
        val event = try {
            eventServiceClient.getEventById(eventId)
        } catch (e: Exception) {
            logger.error("Failed to fetch event details for event $eventId: ${e.message}")
            null
        }

        // Award points for rating (2 points base + bonus for high ratings)
        val bonusPoints = if (rating >= 4) rating - 3 else 0
        val totalPoints = 2 + bonusPoints

        val transaction = PointTransactionDTO(
            userId = userId,
            points = totalPoints,
            activityType = ActivityType.EVENT_RATING,
            eventId = eventId,
            description = "Rated event: ${event?.title ?: "Unknown Event"} ($rating/5)"
        )

        return pointService.addPoints(transaction)
    }

    @Transactional
    override fun recordEventSharing(userId: Long, eventId: Long): PointTransactionResponseDTO {
        logger.info("Recording event sharing for user $userId, event $eventId")

        // Fetch event details if needed
        val event = try {
            eventServiceClient.getEventById(eventId)
        } catch (e: Exception) {
            logger.error("Failed to fetch event details for event $eventId: ${e.message}")
            null
        }

        // Award points for sharing (3 points)
        val transaction = PointTransactionDTO(
            userId = userId,
            points = 3,
            activityType = ActivityType.EVENT_SHARING,
            eventId = eventId,
            description = "Shared event: ${event?.title ?: "Unknown Event"}"
        )

        return pointService.addPoints(transaction)
    }

    override fun getLeaderboards(): List<LeaderboardDTO> {
        return leaderboardService.getAllLeaderboards()
    }

    override fun getLeaderboard(leaderboardId: Long, userId: Long?, limit: Int): LeaderboardResultDTO {
        return leaderboardService.getLeaderboardResults(leaderboardId, userId, limit)
    }

    override fun getUserBadges(userId: Long): List<UserBadgeDTO> {
        return badgeService.getUserBadges(userId)
    }

    override fun getAvailableBadges(): List<BadgeDTO> {
        return badgeService.getAllBadges()
    }

    override fun getUserActivityStats(userId: Long): Map<String, Int> {
        val stats = mutableMapOf<String, Int>()

        // Get user points
        val userPoints = pointService.getUserPoints(userId)
        stats["totalPoints"] = userPoints.totalPoints
        stats["level"] = userPoints.level

        // Get badge count
        val badges = badgeService.getUserBadges(userId)
        stats["badgesEarned"] = badges.size

        // Get activity counts
        val registrations = pointService.getUserTransactionsByActivityType(userId, ActivityType.EVENT_REGISTRATION)
        stats["eventRegistrations"] = registrations.size

        val attendances = pointService.getUserTransactionsByActivityType(userId, ActivityType.EVENT_ATTENDANCE)
        stats["eventAttendances"] = attendances.size

        val ratings = pointService.getUserTransactionsByActivityType(userId, ActivityType.EVENT_RATING)
        stats["eventRatings"] = ratings.size

        val shares = pointService.getUserTransactionsByActivityType(userId, ActivityType.EVENT_SHARING)
        stats["eventShares"] = shares.size

        return stats
    }

    override fun getMostActiveUsers(limit: Int): List<UserPointsDTO> {
        // For simplicity, use the weekly leaderboard
        return leaderboardService.getTopUsersWeekly(limit)
            .map { entry ->
                UserPointsDTO(
                    userId = entry.userId,
                    totalPoints = entry.score,
                    monthlyPoints = entry.score, // Simplified
                    weeklyPoints = entry.score,  // Simplified
                    level = pointService.calculateLevelForPoints(entry.score),
                    nextLevelPoints = 0, // Placeholder
                    levelProgress = 0.0  // Placeholder
                )
            }
    }

    override fun getMostPopularActivities(): Map<ActivityType, Int> {
        // Simplified implementation
        // In a real app, you would query the database for activity counts
        return mapOf(
            ActivityType.EVENT_REGISTRATION to 100,
            ActivityType.EVENT_ATTENDANCE to 80,
            ActivityType.EVENT_RATING to 50,
            ActivityType.EVENT_SHARING to 30
        )
    }
}