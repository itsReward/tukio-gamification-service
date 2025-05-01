package com.tukio.gamificationservice.repository

import com.tukio.gamificationservice.model.UserPoints
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface UserPointsRepository : JpaRepository<UserPoints, Long> {

    fun findByUserId(userId: Long): Optional<UserPoints>

    @Query("""
        SELECT u FROM UserPoints u
        ORDER BY u.totalPoints DESC
        LIMIT :limit
    """)
    fun findTopUsersByTotalPoints(@Param("limit") limit: Int): List<UserPoints>

    @Query("""
        SELECT u FROM UserPoints u
        ORDER BY u.monthlyPoints DESC
        LIMIT :limit
    """)
    fun findTopUsersByMonthlyPoints(@Param("limit") limit: Int): List<UserPoints>

    @Query("""
        SELECT u FROM UserPoints u
        ORDER BY u.weeklyPoints DESC
        LIMIT :limit
    """)
    fun findTopUsersByWeeklyPoints(@Param("limit") limit: Int): List<UserPoints>

    @Query("""
        SELECT u FROM UserPoints u
        ORDER BY u.level DESC, u.totalPoints DESC
        LIMIT :limit
    """)
    fun findTopUsersByLevel(@Param("limit") limit: Int): List<UserPoints>
}