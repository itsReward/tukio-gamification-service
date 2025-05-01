package com.tukio.gamificationservice.controller

import com.tukio.gamificationservice.dto.*
import com.tukio.gamificationservice.model.ActivityType
import com.tukio.gamificationservice.service.GamificationService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/gamification")
class GamificationController(private val gamificationService: GamificationService) {

    @GetMapping("/profile/{userId}")
    fun getUserGamificationProfile(@PathVariable userId: Long): ResponseEntity<UserGamificationProfileDTO> {
        return ResponseEntity.ok(gamificationService.getUserGamificationProfile(userId))
    }

    @PostMapping("/activity")
    fun processActivityEvent(@RequestBody activityEvent: ActivityEventDTO): ResponseEntity<PointTransactionResponseDTO> {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(gamificationService.processActivityEvent(activityEvent))
    }

    @PostMapping("/events/{eventId}/register")
    fun recordEventRegistration(
        @PathVariable eventId: Long,
        @RequestParam userId: Long
    ): ResponseEntity<PointTransactionResponseDTO> {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(gamificationService.recordEventRegistration(userId, eventId))
    }

    @PostMapping("/events/{eventId}/attend")
    fun recordEventAttendance(
        @PathVariable eventId: Long,
        @RequestParam userId: Long
    ): ResponseEntity<PointTransactionResponseDTO> {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(gamificationService.recordEventAttendance(userId, eventId))
    }

    @PostMapping("/events/{eventId}/rate")
    fun recordEventRating(
        @PathVariable eventId: Long,
        @RequestParam userId: Long,
        @RequestParam rating: Int
    ): ResponseEntity<PointTransactionResponseDTO> {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(gamificationService.recordEventRating(userId, eventId, rating))
    }

    @PostMapping("/events/{eventId}/share")
    fun recordEventSharing(
        @PathVariable eventId: Long,
        @RequestParam userId: Long
    ): ResponseEntity<PointTransactionResponseDTO> {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(gamificationService.recordEventSharing(userId, eventId))
    }

    @GetMapping("/leaderboards")
    fun getLeaderboards(): ResponseEntity<List<LeaderboardDTO>> {
        return ResponseEntity.ok(gamificationService.getLeaderboards())
    }

    @GetMapping("/leaderboards/{leaderboardId}")
    fun getLeaderboard(
        @PathVariable leaderboardId: Long,
        @RequestParam(required = false) userId: Long?,
        @RequestParam(defaultValue = "10") limit: Int
    ): ResponseEntity<LeaderboardResultDTO> {
        return ResponseEntity.ok(gamificationService.getLeaderboard(leaderboardId, userId, limit))
    }

    @GetMapping("/badges")
    fun getAvailableBadges(): ResponseEntity<List<BadgeDTO>> {
        return ResponseEntity.ok(gamificationService.getAvailableBadges())
    }

    @GetMapping("/users/{userId}/badges")
    fun getUserBadges(@PathVariable userId: Long): ResponseEntity<List<UserBadgeDTO>> {
        return ResponseEntity.ok(gamificationService.getUserBadges(userId))
    }

    @GetMapping("/users/{userId}/stats")
    fun getUserActivityStats(@PathVariable userId: Long): ResponseEntity<Map<String, Int>> {
        return ResponseEntity.ok(gamificationService.getUserActivityStats(userId))
    }

    @GetMapping("/users/most-active")
    fun getMostActiveUsers(@RequestParam(defaultValue = "10") limit: Int): ResponseEntity<List<UserPointsDTO>> {
        return ResponseEntity.ok(gamificationService.getMostActiveUsers(limit))
    }

    @GetMapping("/activities/popular")
    fun getMostPopularActivities(): ResponseEntity<Map<ActivityType, Int>> {
        return ResponseEntity.ok(gamificationService.getMostPopularActivities())
    }
}