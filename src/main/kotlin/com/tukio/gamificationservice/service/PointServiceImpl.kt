package com.tukio.gamificationservice.service

import com.tukio.gamificationservice.dto.BadgeDTO
import com.tukio.gamificationservice.dto.PointTransactionDTO
import com.tukio.gamificationservice.dto.PointTransactionResponseDTO
import com.tukio.gamificationservice.dto.UserPointsDTO
import com.tukio.gamificationservice.exception.ResourceNotFoundException
import com.tukio.gamificationservice.model.ActivityType
import com.tukio.gamificationservice.model.PointTransaction
import com.tukio.gamificationservice.model.UserPoints
import com.tukio.gamificationservice.repository.PointTransactionRepository
import com.tukio.gamificationservice.repository.UserPointsRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

@Service
class PointServiceImpl(
    private val pointTransactionRepository: PointTransactionRepository,
    private val userPointsRepository: UserPointsRepository,
    private val badgeService: BadgeService
) : PointService {

    private val logger = LoggerFactory.getLogger(PointServiceImpl::class.java)

    override fun getUserPoints(userId: Long): UserPointsDTO {
        // Find or create user points
        val userPoints = findOrCreateUserPoints(userId)

        // Calculate next level info
        val nextLevelPoints = getPointsRequiredForNextLevel(userPoints.level)
        val currentLevelPoints = getPointsRequiredForLevel(userPoints.level)
        val pointsToNextLevel = nextLevelPoints - userPoints.totalPoints

        // Calculate progress percentage
        val levelPointsRange = nextLevelPoints - currentLevelPoints
        val userPointsInLevel = userPoints.totalPoints - currentLevelPoints
        val progress = if (levelPointsRange > 0) {
            (userPointsInLevel.toDouble() / levelPointsRange) * 100.0
        } else {
            100.0
        }

        return UserPointsDTO(
            userId = userPoints.userId,
            totalPoints = userPoints.totalPoints,
            monthlyPoints = userPoints.monthlyPoints,
            weeklyPoints = userPoints.weeklyPoints,
            level = userPoints.level,
            nextLevelPoints = pointsToNextLevel,
            levelProgress = progress
        )
    }

    @Transactional
    override fun addPoints(transactionDTO: PointTransactionDTO): PointTransactionResponseDTO {
        // Create point transaction
        val transaction = PointTransaction(
            userId = transactionDTO.userId,
            points = transactionDTO.points,
            activityType = transactionDTO.activityType,
            eventId = transactionDTO.eventId,
            description = transactionDTO.description
        )

        val savedTransaction = pointTransactionRepository.save(transaction)

        // Update user points
        val userPoints = findOrCreateUserPoints(transactionDTO.userId)
        val oldLevel = userPoints.level

        userPoints.totalPoints += transactionDTO.points
        userPoints.monthlyPoints += transactionDTO.points
        userPoints.weeklyPoints += transactionDTO.points

        // Calculate new level
        val newLevel = calculateLevelForPoints(userPoints.totalPoints)
        userPoints.level = newLevel
        userPoints.updatedAt = LocalDateTime.now()

        userPointsRepository.save(userPoints)

        // Check for new badges earned
        val newBadges = if (transactionDTO.points > 0) {
            badgeService.checkAndAwardBadges(transactionDTO.userId, userPoints.totalPoints)
        } else {
            emptyList()
        }

        // Create level-up transaction if user leveled up
        if (newLevel > oldLevel) {
            val levelUpPoints = 100 * (newLevel - oldLevel) // Bonus points for leveling up
            val levelUpTransaction = PointTransaction(
                userId = transactionDTO.userId,
                points = levelUpPoints,
                activityType = ActivityType.LEVEL_UP,
                description = "Reached level $newLevel"
            )
            pointTransactionRepository.save(levelUpTransaction)

            // Update user points again with level-up bonus
            userPoints.totalPoints += levelUpPoints
            userPoints.monthlyPoints += levelUpPoints
            userPoints.weeklyPoints += levelUpPoints
            userPointsRepository.save(userPoints)
        }

        logger.info("Added ${transactionDTO.points} points for user ${transactionDTO.userId}, new total: ${userPoints.totalPoints}")

        return PointTransactionResponseDTO(
            id = savedTransaction.id,
            userId = savedTransaction.userId,
            points = savedTransaction.points,
            activityType = savedTransaction.activityType,
            eventId = savedTransaction.eventId,
            description = savedTransaction.description,
            timestamp = savedTransaction.timestamp,
            newTotalPoints = userPoints.totalPoints,
            newLevel = userPoints.level,
            badgesEarned = newBadges.map { it.badge }
        )
    }

    override fun getUserTransactions(userId: Long, pageable: Pageable): Page<PointTransactionResponseDTO> {
        return pointTransactionRepository.findByUserId(userId, pageable)
            .map { it.toResponseDTO(0, 0, emptyList()) }
    }

    override fun getUserTransactionsByActivityType(userId: Long, activityType: ActivityType): List<PointTransactionResponseDTO> {
        return pointTransactionRepository.findByUserIdAndActivityType(userId, activityType)
            .map { it.toResponseDTO(0, 0, emptyList()) }
    }

    override fun calculateLevelForPoints(points: Int): Int {
        // Level formula: Level = 1 + floor(sqrt(points / 100))
        // This creates a curve where each level requires progressively more points
        return 1 + (sqrt(points.toDouble() / 100.0)).toInt()
    }

    override fun getPointsRequiredForNextLevel(currentLevel: Int): Int {
        return getPointsRequiredForLevel(currentLevel + 1)
    }

    private fun getPointsRequiredForLevel(level: Int): Int {
        // Inverse of the level formula: Points = 100 * (level - 1)²
        return 100 * (level - 1).toDouble().pow(2.0).toInt()
    }

    @Scheduled(cron = "0 0 0 * * MON") // Every Monday at midnight
    @Transactional
    override fun resetWeeklyPoints() {
        logger.info("Resetting weekly points for all users")

        val users = userPointsRepository.findAll()
        for (user in users) {
            user.weeklyPoints = 0
            user.updatedAt = LocalDateTime.now()
            userPointsRepository.save(user)
        }
    }

    @Scheduled(cron = "0 0 0 1 * *") // First day of each month at midnight
    @Transactional
    override fun resetMonthlyPoints() {
        logger.info("Resetting monthly points for all users")

        val users = userPointsRepository.findAll()
        for (user in users) {
            user.monthlyPoints = 0
            user.updatedAt = LocalDateTime.now()
            userPointsRepository.save(user)
        }
    }

    // Helper methods
    private fun findOrCreateUserPoints(userId: Long): UserPoints {
        return userPointsRepository.findByUserId(userId).orElseGet {
            val newUserPoints = UserPoints(userId = userId)
            userPointsRepository.save(newUserPoints)
        }
    }

    private fun PointTransaction.toResponseDTO(
        newTotalPoints: Int,
        newLevel: Int,
        badgesEarned: List<BadgeDTO>
    ): PointTransactionResponseDTO {
        return PointTransactionResponseDTO(
            id = this.id,
            userId = this.userId,
            points = this.points,
            activityType = this.activityType,
            eventId = this.eventId,
            description = this.description,
            timestamp = this.timestamp,
            newTotalPoints = newTotalPoints,
            newLevel = newLevel,
            badgesEarned = badgesEarned
        )
    }
}