package com.tukio.gamificationservice.service

import com.tukio.gamificationservice.client.UserServiceClient
import com.tukio.gamificationservice.dto.LeaderboardDTO
import com.tukio.gamificationservice.dto.LeaderboardEntryDTO
import com.tukio.gamificationservice.dto.LeaderboardResultDTO
import com.tukio.gamificationservice.dto.UserDTO
import com.tukio.gamificationservice.exception.ResourceNotFoundException
import com.tukio.gamificationservice.model.*
import com.tukio.gamificationservice.repository.LeaderboardEntryRepository
import com.tukio.gamificationservice.repository.LeaderboardRepository
import com.tukio.gamificationservice.repository.PointTransactionRepository
import com.tukio.gamificationservice.repository.UserPointsRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters

@Service
class LeaderboardServiceImpl(
    private val leaderboardRepository: LeaderboardRepository,
    private val leaderboardEntryRepository: LeaderboardEntryRepository,
    private val userPointsRepository: UserPointsRepository,
    private val pointTransactionRepository: PointTransactionRepository,
    private val userServiceClient: UserServiceClient
) : LeaderboardService {

    private val logger = LoggerFactory.getLogger(LeaderboardServiceImpl::class.java)

    override fun getAllLeaderboards(): List<LeaderboardDTO> {
        return leaderboardRepository.findByIsActiveTrue().map { it.toDTO() }
    }

    override fun getLeaderboardById(leaderboardId: Long): LeaderboardDTO {
        val leaderboard = leaderboardRepository.findById(leaderboardId)
            .orElseThrow { ResourceNotFoundException("Leaderboard not found with id: $leaderboardId") }
        return leaderboard.toDTO()
    }

    override fun getLeaderboardEntries(leaderboardId: Long, limit: Int, offset: Int): List<LeaderboardEntryDTO> {
        val pageable = PageRequest.of(offset / limit, limit)
        val entries = leaderboardEntryRepository.findByLeaderboardIdOrderByRankAsc(leaderboardId, pageable)

        // Fetch user details for each entry
        val userIds = entries.map { it.userId }
        val userMap = try {
            userServiceClient.getUsersByIds(userIds).associateBy { it.id }
        } catch (e: Exception) {
            logger.error("Failed to fetch user details: ${e.message}")
            emptyMap<Long, UserDTO>()
        }

        return entries.map { entry ->
            val user = userMap[entry.userId]
            entry.toDTO(user)
        }
    }

    override fun getLeaderboardResults(leaderboardId: Long, userId: Long?, limit: Int): LeaderboardResultDTO {
        val leaderboard = leaderboardRepository.findById(leaderboardId)
            .orElseThrow { ResourceNotFoundException("Leaderboard not found with id: $leaderboardId") }

        val entries = getLeaderboardEntries(leaderboardId, limit, 0)

        val userRank = if (userId != null) {
            getUserRankInLeaderboard(leaderboardId, userId)
        } else {
            null
        }

        return LeaderboardResultDTO(
            leaderboard = leaderboard.toDTO(),
            entries = entries,
            userRank = userRank
        )
    }

    override fun getUserRankInLeaderboard(leaderboardId: Long, userId: Long): LeaderboardEntryDTO? {
        val entry = leaderboardEntryRepository.findByLeaderboardIdAndUserId(leaderboardId, userId)
            .orElse(null) ?: return null

        val user = try {
            userServiceClient.getUserById(userId)
        } catch (e: Exception) {
            logger.error("Failed to fetch user details for user $userId: ${e.message}")
            null
        }

        return entry.toDTO(user)
    }

    override fun getUserRankings(userId: Long): List<LeaderboardEntryDTO> {
        val entries = leaderboardEntryRepository.findByUserId(userId)

        val user = try {
            userServiceClient.getUserById(userId)
        } catch (e: Exception) {
            logger.error("Failed to fetch user details for user $userId: ${e.message}")
            null
        }

        return entries.map { it.toDTO(user) }
    }

    @Transactional
    override fun getOrCreateLeaderboard(
        timeFrame: TimeFrame,
        category: LeaderboardCategory,
        eventCategoryId: Long?
    ): LeaderboardDTO {
        // Try to find existing leaderboard
        val existingLeaderboard = if (eventCategoryId != null) {
            leaderboardRepository.findByCategoryAndEventCategoryIdAndActive(category, eventCategoryId)
        } else {
            leaderboardRepository.findByTimeFrameAndCategoryAndActive(timeFrame, category)
        }

        if (existingLeaderboard.isPresent) {
            return existingLeaderboard.get().toDTO()
        }

        // Create new leaderboard
        val leaderboardName = buildLeaderboardName(timeFrame, category, eventCategoryId)
        val description = buildLeaderboardDescription(timeFrame, category, eventCategoryId)

        val leaderboard = Leaderboard(
            name = leaderboardName,
            description = description,
            timeFrame = timeFrame,
            category = category,
            eventCategoryId = eventCategoryId,
            isActive = true
        )

        val savedLeaderboard = leaderboardRepository.save(leaderboard)

        // Initialize leaderboard entries
        updateLeaderboard(savedLeaderboard.id)

        return savedLeaderboard.toDTO()
    }

    @Transactional
    override fun updateLeaderboard(leaderboardId: Long) {
        val leaderboard = leaderboardRepository.findById(leaderboardId)
            .orElseThrow { ResourceNotFoundException("Leaderboard not found with id: $leaderboardId") }

        // Clear existing entries
        leaderboardEntryRepository.deleteAllByLeaderboardId(leaderboardId)

        // Get user scores based on leaderboard category and time frame
        val userScores = when (leaderboard.category) {
            LeaderboardCategory.OVERALL -> getOverallLeaderboardScores(leaderboard.timeFrame)
            LeaderboardCategory.EVENT_ATTENDANCE -> getAttendanceLeaderboardScores(leaderboard.timeFrame)
            LeaderboardCategory.BADGES_EARNED -> getBadgesLeaderboardScores(leaderboard.timeFrame)
            LeaderboardCategory.CATEGORY_SPECIFIC -> getCategorySpecificLeaderboardScores(
                leaderboard.timeFrame,
                leaderboard.eventCategoryId ?: 0
            )
            LeaderboardCategory.DIVERSE_PARTICIPATION -> getDiverseParticipationLeaderboardScores(leaderboard.timeFrame)
        }

        // Create new entries with ranks
        var rank = 1
        for ((userId, score) in userScores) {
            val entry = LeaderboardEntry(
                leaderboard = leaderboard,
                userId = userId,
                rank = rank++,
                score = score
            )
            leaderboardEntryRepository.save(entry)
        }

        // Update last updated timestamp
        leaderboard.lastUpdated = LocalDateTime.now()
        leaderboardRepository.save(leaderboard)

        logger.info("Updated leaderboard ${leaderboard.name} with ${userScores.size} entries")
    }

    @Scheduled(cron = "0 0 4 * * *") // Run every day at 4 AM
    @Transactional
    override fun updateAllLeaderboards() {
        logger.info("Updating all active leaderboards")

        val activeLeaderboards = leaderboardRepository.findByIsActiveTrue()
        for (leaderboard in activeLeaderboards) {
            try {
                updateLeaderboard(leaderboard.id)
            } catch (e: Exception) {
                logger.error("Failed to update leaderboard ${leaderboard.id}: ${e.message}")
            }
        }
    }

    override fun getTopUsersWeekly(limit: Int): List<LeaderboardEntryDTO> {
        // Get or create weekly leaderboard
        val leaderboard = getOrCreateLeaderboard(
            TimeFrame.WEEKLY,
            LeaderboardCategory.OVERALL
        )

        return getLeaderboardEntries(leaderboard.id!!, limit, 0)
    }

    override fun getTopUsersMonthly(limit: Int): List<LeaderboardEntryDTO> {
        // Get or create monthly leaderboard
        val leaderboard = getOrCreateLeaderboard(
            TimeFrame.MONTHLY,
            LeaderboardCategory.OVERALL
        )

        return getLeaderboardEntries(leaderboard.id!!, limit, 0)
    }

    override fun getTopUsersAllTime(limit: Int): List<LeaderboardEntryDTO> {
        // Get or create all-time leaderboard
        val leaderboard = getOrCreateLeaderboard(
            TimeFrame.ALL_TIME,
            LeaderboardCategory.OVERALL
        )

        return getLeaderboardEntries(leaderboard.id!!, limit, 0)
    }

    // Helper methods for leaderboard score calculations
    private fun getOverallLeaderboardScores(timeFrame: TimeFrame): Map<Long, Int> {
        val scores = mutableMapOf<Long, Int>()

        when (timeFrame) {
            TimeFrame.WEEKLY -> {
                // Use weekly points from user_points table
                val users = userPointsRepository.findAll()
                for (user in users) {
                    if (user.weeklyPoints > 0) {
                        scores[user.userId] = user.weeklyPoints
                    }
                }
            }
            TimeFrame.MONTHLY -> {
                // Use monthly points from user_points table
                val users = userPointsRepository.findAll()
                for (user in users) {
                    if (user.monthlyPoints > 0) {
                        scores[user.userId] = user.monthlyPoints
                    }
                }
            }
            TimeFrame.SEMESTER -> {
                // Calculate points for the current semester (roughly 4 months)
                val startDate = LocalDateTime.now().minusMonths(4)
                calculateScoresFromPointTransactions(startDate, scores)
            }
            TimeFrame.YEARLY -> {
                // Calculate points for the current year
                val startDate = LocalDateTime.now().withDayOfYear(1).withHour(0).withMinute(0).withSecond(0)
                calculateScoresFromPointTransactions(startDate, scores)
            }
            TimeFrame.ALL_TIME -> {
                // Use total points from user_points table
                val users = userPointsRepository.findAll()
                for (user in users) {
                    if (user.totalPoints > 0) {
                        scores[user.userId] = user.totalPoints
                    }
                }
            }
        }

        // Sort by score in descending order
        return scores.toList().sortedByDescending { it.second }.toMap()
    }

    private fun getAttendanceLeaderboardScores(timeFrame: TimeFrame): Map<Long, Int> {
        val scores = mutableMapOf<Long, Int>()

        // Define time range based on timeframe
        val startDate = getStartDateForTimeFrame(timeFrame)
        if (startDate != null) {
            val users = userPointsRepository.findAll()

            for (user in users) {
                val count = pointTransactionRepository.countActivitiesByUserIdAndTypeInTimeframe(
                    user.userId,
                    ActivityType.EVENT_ATTENDANCE,
                    startDate,
                    LocalDateTime.now()
                )

                if (count > 0) {
                    scores[user.userId] = count.toInt()
                }
            }
        }

        // Sort by attendance count in descending order
        return scores.toList().sortedByDescending { it.second }.toMap()
    }

    private fun getBadgesLeaderboardScores(timeFrame: TimeFrame): Map<Long, Int> {
        // For simplicity, this just counts total badges
        // In a real implementation, you would count badges earned within the time frame
        val scores = mutableMapOf<Long, Int>()

        return scores
    }

    private fun getCategorySpecificLeaderboardScores(timeFrame: TimeFrame, categoryId: Long): Map<Long, Int> {
        // For simplicity, we return an empty map
        // In a real implementation, you would count points from events in the specific category
        return emptyMap()
    }

    private fun getDiverseParticipationLeaderboardScores(timeFrame: TimeFrame): Map<Long, Int> {
        // For simplicity, we return an empty map
        // In a real implementation, you would calculate a diversity score based on
        // participation in different event categories
        return emptyMap()
    }

    private fun calculateScoresFromPointTransactions(startDate: LocalDateTime, scores: MutableMap<Long, Int>) {
        val users = userPointsRepository.findAll()

        for (user in users) {
            val points = pointTransactionRepository.sumPointsByUserIdSince(
                user.userId,
                startDate
            ) ?: 0

            if (points > 0) {
                scores[user.userId] = points
            }
        }
    }

    private fun getStartDateForTimeFrame(timeFrame: TimeFrame): LocalDateTime? {
        return when (timeFrame) {
            TimeFrame.WEEKLY -> LocalDateTime.now().with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
            TimeFrame.MONTHLY -> LocalDateTime.now().withDayOfMonth(1)
            TimeFrame.SEMESTER -> LocalDateTime.now().minusMonths(4)
            TimeFrame.YEARLY -> LocalDateTime.now().withDayOfYear(1)
            TimeFrame.ALL_TIME -> null // All time doesn't have a start date
        }
    }

    private fun buildLeaderboardName(
        timeFrame: TimeFrame,
        category: LeaderboardCategory,
        eventCategoryId: Long?
    ): String {
        val timeFrameStr = timeFrame.name.lowercase().capitalize()
        val categoryStr = category.name.lowercase().replace("_", " ").capitalize()

        return if (eventCategoryId != null) {
            "$timeFrameStr $categoryStr - Category $eventCategoryId"
        } else {
            "$timeFrameStr $categoryStr"
        }
    }

    private fun buildLeaderboardDescription(
        timeFrame: TimeFrame,
        category: LeaderboardCategory,
        eventCategoryId: Long?
    ): String {
        val timeFrameStr = timeFrame.name.lowercase().capitalize()
        val categoryStr = when (category) {
            LeaderboardCategory.OVERALL -> "overall points"
            LeaderboardCategory.EVENT_ATTENDANCE -> "event attendance"
            LeaderboardCategory.BADGES_EARNED -> "badges earned"
            LeaderboardCategory.CATEGORY_SPECIFIC -> {
                val categoryIdStr = eventCategoryId?.toString() ?: "unknown"
                "points earned in category $categoryIdStr"
            }
            LeaderboardCategory.DIVERSE_PARTICIPATION -> "participation in diverse event categories"
        }

        return "Top users for $timeFrameStr $categoryStr leaderboard"
    }

    // Extension functions for mapping entities to DTOs
    private fun Leaderboard.toDTO(): LeaderboardDTO {
        return LeaderboardDTO(
            id = this.id,
            name = this.name,
            description = this.description,
            timeFrame = this.timeFrame,
            category = this.category,
            eventCategoryId = this.eventCategoryId,
            isActive = this.isActive,
            lastUpdated = this.lastUpdated
        )
    }

    private fun LeaderboardEntry.toDTO(user: UserDTO?): LeaderboardEntryDTO {
        return LeaderboardEntryDTO(
            leaderboardId = this.leaderboard.id,
            leaderboardName = this.leaderboard.name,
            userId = this.userId,
            username = user?.username ?: "Unknown User",
            firstName = user?.firstName ?: "Unknown",
            lastName = user?.lastName ?: "User",
            profilePictureUrl = user?.profilePictureUrl,
            rank = this.rank,
            score = this.score
        )
    }
}