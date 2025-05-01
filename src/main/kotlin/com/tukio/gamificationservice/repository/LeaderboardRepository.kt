package com.tukio.gamificationservice.repository

import com.tukio.gamificationservice.model.Leaderboard
import com.tukio.gamificationservice.model.LeaderboardCategory
import com.tukio.gamificationservice.model.TimeFrame
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface LeaderboardRepository : JpaRepository<Leaderboard, Long> {

    fun findByName(name: String): Optional<Leaderboard>

    fun findByIsActiveTrue(): List<Leaderboard>

    fun findByTimeFrameAndIsActiveTrue(timeFrame: TimeFrame): List<Leaderboard>

    fun findByCategoryAndIsActiveTrue(category: LeaderboardCategory): List<Leaderboard>

    @Query("""
        SELECT l FROM Leaderboard l
        WHERE l.timeFrame = :timeFrame
        AND l.category = :category
        AND l.isActive = true
    """)
    fun findByTimeFrameAndCategoryAndActive(
        @Param("timeFrame") timeFrame: TimeFrame,
        @Param("category") category: LeaderboardCategory
    ): Optional<Leaderboard>

    @Query("""
        SELECT l FROM Leaderboard l
        WHERE l.category = :category
        AND l.eventCategoryId = :eventCategoryId
        AND l.isActive = true
    """)
    fun findByCategoryAndEventCategoryIdAndActive(
        @Param("category") category: LeaderboardCategory,
        @Param("eventCategoryId") eventCategoryId: Long
    ): Optional<Leaderboard>
}