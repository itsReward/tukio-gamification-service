package com.tukio.gamificationservice.repository

import com.tukio.gamificationservice.model.Badge
import com.tukio.gamificationservice.model.BadgeCategory
import com.tukio.gamificationservice.model.BadgeTier
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface BadgeRepository : JpaRepository<Badge, Long> {

    fun findByName(name: String): Badge?

    fun findByCategory(category: BadgeCategory): List<Badge>

    fun findByTier(tier: BadgeTier): List<Badge>

    fun findByCategoryAndTier(category: BadgeCategory, tier: BadgeTier): List<Badge>

    @Query("""
        SELECT b FROM Badge b
        WHERE b.requiredPoints <= :points
        ORDER BY b.requiredPoints DESC
    """)
    fun findBadgesEligibleByPoints(@Param("points") points: Int): List<Badge>

    @Query("""
        SELECT b FROM Badge b
        WHERE b.category = :category
        AND b.requiredPoints <= :points
        ORDER BY b.requiredPoints DESC
    """)
    fun findBadgesEligibleByPointsAndCategory(
        @Param("points") points: Int,
        @Param("category") category: BadgeCategory
    ): List<Badge>
}