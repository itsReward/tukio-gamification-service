package com.tukio.gamificationservice.repository

import com.tukio.gamificationservice.model.ActivityType
import com.tukio.gamificationservice.model.PointTransaction
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface PointTransactionRepository : JpaRepository<PointTransaction, Long> {

    fun findByUserId(userId: Long, pageable: Pageable): Page<PointTransaction>

    fun findByUserIdAndActivityType(userId: Long, activityType: ActivityType): List<PointTransaction>

    @Query("""
        SELECT pt FROM PointTransaction pt
        WHERE pt.userId = :userId
        AND pt.timestamp >= :since
        ORDER BY pt.timestamp DESC
    """)
    fun findRecentTransactionsByUserId(
        @Param("userId") userId: Long,
        @Param("since") since: LocalDateTime,
        pageable: Pageable
    ): Page<PointTransaction>

    @Query("""
        SELECT pt FROM PointTransaction pt
        WHERE pt.eventId = :eventId
        ORDER BY pt.timestamp DESC
    """)
    fun findByEventId(@Param("eventId") eventId: Long): List<PointTransaction>

    @Query("""
        SELECT SUM(pt.points) FROM PointTransaction pt
        WHERE pt.userId = :userId
    """)
    fun sumPointsByUserId(@Param("userId") userId: Long): Int?

    @Query("""
        SELECT SUM(pt.points) FROM PointTransaction pt
        WHERE pt.userId = :userId
        AND pt.timestamp >= :since
    """)
    fun sumPointsByUserIdSince(
        @Param("userId") userId: Long,
        @Param("since") since: LocalDateTime
    ): Int?

    @Query("""
        SELECT COUNT(DISTINCT pt.eventId) FROM PointTransaction pt
        WHERE pt.userId = :userId
        AND pt.eventId IS NOT NULL
        AND pt.activityType = :activityType
    """)
    fun countDistinctEventsByUserIdAndActivityType(
        @Param("userId") userId: Long,
        @Param("activityType") activityType: ActivityType
    ): Long

    @Query("""
        SELECT COUNT(*) FROM PointTransaction pt
        WHERE pt.userId = :userId
        AND pt.eventId IS NOT NULL
        AND pt.activityType = :activityType
        AND pt.timestamp BETWEEN :startDate AND :endDate
    """)
    fun countActivitiesByUserIdAndTypeInTimeframe(
        @Param("userId") userId: Long,
        @Param("activityType") activityType: ActivityType,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): Long
}