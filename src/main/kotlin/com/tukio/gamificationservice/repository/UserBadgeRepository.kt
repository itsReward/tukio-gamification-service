package com.tukio.gamificationservice.repository

import com.tukio.gamificationservice.model.BadgeCategory
import com.tukio.gamificationservice.model.UserBadge
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface UserBadgeRepository : JpaRepository<UserBadge, Long> {

    fun findByUserId(userId: Long): List<UserBadge>

    @Query("""
        SELECT ub FROM UserBadge ub
        WHERE ub.userId = :userId
        AND ub.badge.id = :badgeId
    """)
    fun findByUserIdAndBadgeId(
        @Param("userId") userId: Long,
        @Param("badgeId") badgeId: Long
    ): UserBadge?

    @Query("""
        SELECT ub FROM UserBadge ub
        WHERE ub.userId = :userId
        AND ub.badge.category = :category
    """)
    fun findByUserIdAndBadgeCategory(
        @Param("userId") userId: Long,
        @Param("category") category: BadgeCategory
    ): List<UserBadge>

    @Query("""
        SELECT ub FROM UserBadge ub
        WHERE ub.userId = :userId
        AND ub.awardedAt >= :since
        ORDER BY ub.awardedAt DESC
    """)
    fun findRecentUserBadges(
        @Param("userId") userId: Long,
        @Param("since") since: LocalDateTime
    ): List<UserBadge>

    @Query("""
        SELECT COUNT(DISTINCT ub.userId) FROM UserBadge ub
        WHERE ub.badge.id = :badgeId
    """)
    fun countUsersWithBadge(@Param("badgeId") badgeId: Long): Long

    @Query("""
        SELECT COUNT(ub) FROM UserBadge ub
        WHERE ub.userId = :userId
    """)
    fun countBadgesForUser(@Param("userId") userId: Long): Long
}