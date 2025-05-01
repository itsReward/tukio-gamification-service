package com.tukio.gamificationservice.repository

import com.tukio.gamificationservice.model.LeaderboardEntry
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface LeaderboardEntryRepository : JpaRepository<LeaderboardEntry, Long> {

    @Query("""
        SELECT le FROM LeaderboardEntry le
        WHERE le.leaderboard.id = :leaderboardId
        ORDER BY le.rank ASC
    """)
    fun findByLeaderboardIdOrderByRankAsc(
        @Param("leaderboardId") leaderboardId: Long,
        pageable: Pageable
    ): List<LeaderboardEntry>

    @Query("""
        SELECT le FROM LeaderboardEntry le
        WHERE le.leaderboard.id = :leaderboardId
        AND le.userId = :userId
    """)
    fun findByLeaderboardIdAndUserId(
        @Param("leaderboardId") leaderboardId: Long,
        @Param("userId") userId: Long
    ): Optional<LeaderboardEntry>

    @Query("""
        SELECT le FROM LeaderboardEntry le
        WHERE le.userId = :userId
        ORDER BY le.leaderboard.id ASC
    """)
    fun findByUserId(@Param("userId") userId: Long): List<LeaderboardEntry>

    @Query("""
        SELECT COUNT(le) FROM LeaderboardEntry le
        WHERE le.leaderboard.id = :leaderboardId
    """)
    fun countEntriesByLeaderboardId(@Param("leaderboardId") leaderboardId: Long): Long

    @Query("""
        SELECT COUNT(le) FROM LeaderboardEntry le
        WHERE le.leaderboard.id = :leaderboardId
        AND le.score > :score
    """)
    fun countEntriesWithHigherScore(
        @Param("leaderboardId") leaderboardId: Long,
        @Param("score") score: Int
    ): Long

    @Query("""
        DELETE FROM LeaderboardEntry le
        WHERE le.leaderboard.id = :leaderboardId
    """)
    fun deleteAllByLeaderboardId(@Param("leaderboardId") leaderboardId: Long)
}