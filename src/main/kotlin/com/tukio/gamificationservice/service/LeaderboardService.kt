package com.tukio.gamificationservice.service

import com.tukio.gamificationservice.dto.LeaderboardDTO
import com.tukio.gamificationservice.dto.LeaderboardEntryDTO
import com.tukio.gamificationservice.dto.LeaderboardResultDTO
import com.tukio.gamificationservice.model.LeaderboardCategory
import com.tukio.gamificationservice.model.TimeFrame

interface LeaderboardService {

    fun getAllLeaderboards(): List<LeaderboardDTO>

    fun getLeaderboardById(leaderboardId: Long): LeaderboardDTO

    fun getLeaderboardEntries(leaderboardId: Long, limit: Int, offset: Int): List<LeaderboardEntryDTO>

    fun getLeaderboardResults(leaderboardId: Long, userId: Long?, limit: Int): LeaderboardResultDTO

    fun getUserRankInLeaderboard(leaderboardId: Long, userId: Long): LeaderboardEntryDTO?

    fun getUserRankings(userId: Long): List<LeaderboardEntryDTO>

    fun getOrCreateLeaderboard(
        timeFrame: TimeFrame,
        category: LeaderboardCategory,
        eventCategoryId: Long? = null
    ): LeaderboardDTO

    fun updateLeaderboard(leaderboardId: Long)

    fun updateAllLeaderboards()

    fun getTopUsersWeekly(limit: Int): List<LeaderboardEntryDTO>

    fun getTopUsersMonthly(limit: Int): List<LeaderboardEntryDTO>

    fun getTopUsersAllTime(limit: Int): List<LeaderboardEntryDTO>
}