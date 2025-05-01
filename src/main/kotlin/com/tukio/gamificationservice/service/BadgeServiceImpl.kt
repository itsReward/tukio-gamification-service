package com.tukio.gamificationservice.service

import com.tukio.gamificationservice.dto.BadgeDTO
import com.tukio.gamificationservice.dto.UserBadgeDTO
import com.tukio.gamificationservice.exception.ResourceNotFoundException
import com.tukio.gamificationservice.model.*
import com.tukio.gamificationservice.repository.BadgeRepository
import com.tukio.gamificationservice.repository.PointTransactionRepository
import com.tukio.gamificationservice.repository.UserBadgeRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class BadgeServiceImpl(
    private val badgeRepository: BadgeRepository,
    private val userBadgeRepository: UserBadgeRepository,
    private val pointTransactionRepository: PointTransactionRepository
) : BadgeService {

    private val logger = LoggerFactory.getLogger(BadgeServiceImpl::class.java)

    override fun getAllBadges(): List<BadgeDTO> {
        return badgeRepository.findAll().map { it.toDTO() }
    }

    override fun getBadgeById(badgeId: Long): BadgeDTO {
        val badge = badgeRepository.findById(badgeId)
            .orElseThrow { ResourceNotFoundException("Badge not found with id: $badgeId") }
        return badge.toDTO()
    }

    override fun getBadgesByCategory(category: BadgeCategory): List<BadgeDTO> {
        return badgeRepository.findByCategory(category).map { it.toDTO() }
    }

    override fun getBadgesByTier(tier: BadgeTier): List<BadgeDTO> {
        return badgeRepository.findByTier(tier).map { it.toDTO() }
    }

    override fun getUserBadges(userId: Long): List<UserBadgeDTO> {
        return userBadgeRepository.findByUserId(userId).map { it.toDTO() }
    }

    override fun getUserBadgesByCategory(userId: Long, category: BadgeCategory): List<UserBadgeDTO> {
        return userBadgeRepository.findByUserIdAndBadgeCategory(userId, category).map { it.toDTO() }
    }

    @Transactional
    override fun awardBadge(userId: Long, badgeId: Long, eventId: Long?): UserBadgeDTO {
        // Check if user already has this badge
        val existingBadge = userBadgeRepository.findByUserIdAndBadgeId(userId, badgeId)
        if (existingBadge != null) {
            return existingBadge.toDTO()
        }

        // Get the badge
        val badge = badgeRepository.findById(badgeId)
            .orElseThrow { ResourceNotFoundException("Badge not found with id: $badgeId") }

        // Create user badge
        val userBadge = UserBadge(
            userId = userId,
            badge = badge,
            eventId = eventId
        )

        val savedUserBadge = userBadgeRepository.save(userBadge)

        // Create point transaction for badge award
        val pointTransaction = PointTransaction(
            userId = userId,
            points = 50, // Award points for earning a badge
            activityType = ActivityType.BADGE_EARNED,
            description = "Earned badge: ${badge.name}"
        )
        pointTransactionRepository.save(pointTransaction)

        logger.info("Awarded badge '${badge.name}' to user $userId")

        return savedUserBadge.toDTO()
    }

    @Transactional
    override fun checkAndAwardBadges(userId: Long, totalPoints: Int): List<UserBadgeDTO> {
        // Get badges that the user is eligible for based on points
        val eligibleBadges = badgeRepository.findBadgesEligibleByPoints(totalPoints)

        // Get badges the user already has
        val userBadges = userBadgeRepository.findByUserId(userId)
        val userBadgeIds = userBadges.map { it.badge.id }.toSet()

        // Filter out badges the user already has
        val newEligibleBadges = eligibleBadges.filter { !userBadgeIds.contains(it.id) }

        // Award new badges
        val awardedBadges = mutableListOf<UserBadgeDTO>()
        for (badge in newEligibleBadges) {
            val userBadge = awardBadge(userId, badge.id)
            awardedBadges.add(userBadge)
        }

        return awardedBadges
    }

    override fun getBadgeProgress(userId: Long, badgeId: Long): Double {
        // Get the badge
        val badge = badgeRepository.findById(badgeId)
            .orElseThrow { ResourceNotFoundException("Badge not found with id: $badgeId") }

        // Check if user already has this badge
        val existingBadge = userBadgeRepository.findByUserIdAndBadgeId(userId, badgeId)
        if (existingBadge != null) {
            return 1.0 // 100% complete
        }

        // Calculate progress based on badge category
        return when (badge.category) {
            BadgeCategory.ATTENDANCE -> calculateAttendanceProgress(userId, badge)
            BadgeCategory.REGISTRATION -> calculateRegistrationProgress(userId, badge)
            BadgeCategory.RATING -> calculateRatingProgress(userId, badge)
            BadgeCategory.DIVERSITY -> calculateDiversityProgress(userId, badge)
            BadgeCategory.PARTICIPATION -> calculateParticipationProgress(userId, badge)
            BadgeCategory.ORGANIZATION -> calculateOrganizationProgress(userId, badge)
            BadgeCategory.SOCIAL -> calculateSocialProgress(userId, badge)
            BadgeCategory.MILESTONE -> calculateMilestoneProgress(userId, badge)
        }
    }

    override fun getRecentUserBadges(userId: Long, limit: Int): List<UserBadgeDTO> {
        val since = LocalDateTime.now().minusMonths(1) // Consider badges from the last month
        return userBadgeRepository.findRecentUserBadges(userId, since)
            .take(limit)
            .map { it.toDTO() }
    }

    // Helper methods for calculating badge progress
    private fun calculateAttendanceProgress(userId: Long, badge: Badge): Double {
        val requiredAttendances = badge.requiredPoints / 10 // Assuming 10 points per attendance
        val actualAttendances = pointTransactionRepository.countDistinctEventsByUserIdAndActivityType(
            userId, ActivityType.EVENT_ATTENDANCE
        )
        return (actualAttendances.toDouble() / requiredAttendances).coerceAtMost(1.0)
    }

    private fun calculateRegistrationProgress(userId: Long, badge: Badge): Double {
        val requiredRegistrations = badge.requiredPoints / 5 // Assuming 5 points per registration
        val actualRegistrations = pointTransactionRepository.countDistinctEventsByUserIdAndActivityType(
            userId, ActivityType.EVENT_REGISTRATION
        )
        return (actualRegistrations.toDouble() / requiredRegistrations).coerceAtMost(1.0)
    }

    private fun calculateRatingProgress(userId: Long, badge: Badge): Double {
        val requiredRatings = badge.requiredPoints / 2 // Assuming 2 points per rating
        val actualRatings = pointTransactionRepository.countDistinctEventsByUserIdAndActivityType(
            userId, ActivityType.EVENT_RATING
        )
        return (actualRatings.toDouble() / requiredRatings).coerceAtMost(1.0)
    }

    private fun calculateDiversityProgress(userId: Long, badge: Badge): Double {
        // Simplified implementation
        // In a real app, you would count unique event categories the user has attended
        return 0.5 // Default to 50% progress for demonstration
    }

    private fun calculateParticipationProgress(userId: Long, badge: Badge): Double {
        // Look at total points as a measure of participation
        val pointsNeeded = badge.requiredPoints
        val userPoints = pointTransactionRepository.sumPointsByUserId(userId) ?: 0
        return (userPoints.toDouble() / pointsNeeded).coerceAtMost(1.0)
    }

    private fun calculateOrganizationProgress(userId: Long, badge: Badge): Double {
        // Simplified implementation
        // In a real app, you would track events organized by the user
        return 0.25 // Default to 25% progress for demonstration
    }

    private fun calculateSocialProgress(userId: Long, badge: Badge): Double {
        val requiredShares = badge.requiredPoints / 3 // Assuming 3 points per share
        val actualShares = pointTransactionRepository.countDistinctEventsByUserIdAndActivityType(
            userId, ActivityType.EVENT_SHARING
        )
        return (actualShares.toDouble() / requiredShares).coerceAtMost(1.0)
    }

    private fun calculateMilestoneProgress(userId: Long, badge: Badge): Double {
        // For milestone badges, simply check if user has enough points
        val userPoints = pointTransactionRepository.sumPointsByUserId(userId) ?: 0
        return (userPoints.toDouble() / badge.requiredPoints).coerceAtMost(1.0)
    }

    // Extension functions for mapping entities to DTOs
    private fun Badge.toDTO(): BadgeDTO {
        return BadgeDTO(
            id = this.id,
            name = this.name,
            description = this.description,
            imageUrl = this.imageUrl,
            requiredPoints = this.requiredPoints,
            category = this.category,
            tier = this.tier
        )
    }

    private fun UserBadge.toDTO(): UserBadgeDTO {
        return UserBadgeDTO(
            id = this.id,
            userId = this.userId,
            badge = this.badge.toDTO(),
            awardedAt = this.awardedAt,
            eventId = this.eventId
        )
    }
}