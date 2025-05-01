package com.tukio.gamificationservice.service

import com.tukio.gamificationservice.dto.BadgeDTO
import com.tukio.gamificationservice.dto.UserBadgeDTO
import com.tukio.gamificationservice.model.BadgeCategory
import com.tukio.gamificationservice.model.BadgeTier

interface BadgeService {

    fun getAllBadges(): List<BadgeDTO>

    fun getBadgeById(badgeId: Long): BadgeDTO

    fun getBadgesByCategory(category: BadgeCategory): List<BadgeDTO>

    fun getBadgesByTier(tier: BadgeTier): List<BadgeDTO>

    fun getUserBadges(userId: Long): List<UserBadgeDTO>

    fun getUserBadgesByCategory(userId: Long, category: BadgeCategory): List<UserBadgeDTO>

    fun awardBadge(userId: Long, badgeId: Long, eventId: Long? = null): UserBadgeDTO

    fun checkAndAwardBadges(userId: Long, totalPoints: Int): List<UserBadgeDTO>

    fun getBadgeProgress(userId: Long, badgeId: Long): Double

    fun getRecentUserBadges(userId: Long, limit: Int): List<UserBadgeDTO>
}