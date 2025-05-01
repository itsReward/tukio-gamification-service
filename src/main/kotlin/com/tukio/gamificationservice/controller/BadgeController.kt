package com.tukio.gamificationservice.controller

import com.tukio.gamificationservice.dto.BadgeDTO
import com.tukio.gamificationservice.dto.UserBadgeDTO
import com.tukio.gamificationservice.model.BadgeCategory
import com.tukio.gamificationservice.model.BadgeTier
import com.tukio.gamificationservice.service.BadgeService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/badges")
class BadgeController(private val badgeService: BadgeService) {

    @GetMapping
    fun getAllBadges(): ResponseEntity<List<BadgeDTO>> {
        return ResponseEntity.ok(badgeService.getAllBadges())
    }

    @GetMapping("/{badgeId}")
    fun getBadgeById(@PathVariable badgeId: Long): ResponseEntity<BadgeDTO> {
        return ResponseEntity.ok(badgeService.getBadgeById(badgeId))
    }

    @GetMapping("/category/{category}")
    fun getBadgesByCategory(@PathVariable category: String): ResponseEntity<List<BadgeDTO>> {
        val badgeCategory = try {
            BadgeCategory.valueOf(category.uppercase())
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.badRequest().build()
        }

        return ResponseEntity.ok(badgeService.getBadgesByCategory(badgeCategory))
    }

    @GetMapping("/tier/{tier}")
    fun getBadgesByTier(@PathVariable tier: String): ResponseEntity<List<BadgeDTO>> {
        val badgeTier = try {
            BadgeTier.valueOf(tier.uppercase())
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.badRequest().build()
        }

        return ResponseEntity.ok(badgeService.getBadgesByTier(badgeTier))
    }

    @GetMapping("/user/{userId}")
    fun getUserBadges(@PathVariable userId: Long): ResponseEntity<List<UserBadgeDTO>> {
        return ResponseEntity.ok(badgeService.getUserBadges(userId))
    }

    @GetMapping("/user/{userId}/category/{category}")
    fun getUserBadgesByCategory(
        @PathVariable userId: Long,
        @PathVariable category: String
    ): ResponseEntity<List<UserBadgeDTO>> {
        val badgeCategory = try {
            BadgeCategory.valueOf(category.uppercase())
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.badRequest().build()
        }

        return ResponseEntity.ok(badgeService.getUserBadgesByCategory(userId, badgeCategory))
    }

    @PostMapping("/award")
    fun awardBadge(
        @RequestParam userId: Long,
        @RequestParam badgeId: Long,
        @RequestParam(required = false) eventId: Long?
    ): ResponseEntity<UserBadgeDTO> {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(badgeService.awardBadge(userId, badgeId, eventId))
    }

    @GetMapping("/user/{userId}/progress/{badgeId}")
    fun getBadgeProgress(
        @PathVariable userId: Long,
        @PathVariable badgeId: Long
    ): ResponseEntity<Map<String, Double>> {
        val progress = badgeService.getBadgeProgress(userId, badgeId)
        return ResponseEntity.ok(mapOf("progress" to progress))
    }

    @GetMapping("/user/{userId}/recent")
    fun getRecentUserBadges(
        @PathVariable userId: Long,
        @RequestParam(defaultValue = "5") limit: Int
    ): ResponseEntity<List<UserBadgeDTO>> {
        return ResponseEntity.ok(badgeService.getRecentUserBadges(userId, limit))
    }
}