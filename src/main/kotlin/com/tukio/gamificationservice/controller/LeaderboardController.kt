package com.tukio.gamificationservice.controller

import com.tukio.gamificationservice.dto.LeaderboardDTO
import com.tukio.gamificationservice.dto.LeaderboardEntryDTO
import com.tukio.gamificationservice.dto.LeaderboardResultDTO
import com.tukio.gamificationservice.model.LeaderboardCategory
import com.tukio.gamificationservice.model.TimeFrame
import com.tukio.gamificationservice.service.LeaderboardService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/leaderboards")
class LeaderboardController(private val leaderboardService: LeaderboardService) {

    @GetMapping
    fun getAllLeaderboards(): ResponseEntity<List<LeaderboardDTO>> {
        return ResponseEntity.ok(leaderboardService.getAllLeaderboards())
    }

    @GetMapping("/{leaderboardId}")
    fun getLeaderboardById(@PathVariable leaderboardId: Long): ResponseEntity<LeaderboardDTO> {
        return ResponseEntity.ok(leaderboardService.getLeaderboardById(leaderboardId))
    }

    @GetMapping("/{leaderboardId}/entries")
    fun getLeaderboardEntries(
        @PathVariable leaderboardId: Long,
        @RequestParam(defaultValue = "10") limit: Int,
        @RequestParam(defaultValue = "0") offset: Int
    ): ResponseEntity<List<LeaderboardEntryDTO>> {
        return ResponseEntity.ok(leaderboardService.getLeaderboardEntries(leaderboardId, limit, offset))
    }

    @GetMapping("/{leaderboardId}/results")
    fun getLeaderboardResults(
        @PathVariable leaderboardId: Long,
        @RequestParam(required = false) userId: Long?,
        @RequestParam(defaultValue = "10") limit: Int
    ): ResponseEntity<LeaderboardResultDTO> {
        return ResponseEntity.ok(leaderboardService.getLeaderboardResults(leaderboardId, userId, limit))
    }

    @GetMapping("/user/{userId}")
    fun getUserRankings(@PathVariable userId: Long): ResponseEntity<List<LeaderboardEntryDTO>> {
        return ResponseEntity.ok(leaderboardService.getUserRankings(userId))
    }

    @GetMapping("/user/{userId}/rank/{leaderboardId}")
    fun getUserRankInLeaderboard(
        @PathVariable userId: Long,
        @PathVariable leaderboardId: Long
    ): ResponseEntity<LeaderboardEntryDTO> {
        val rank = leaderboardService.getUserRankInLeaderboard(leaderboardId, userId)
        return if (rank != null) {
            ResponseEntity.ok(rank)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping("/create")
    fun createLeaderboard(
        @RequestParam timeFrame: String,
        @RequestParam category: String,
        @RequestParam(required = false) eventCategoryId: Long?
    ): ResponseEntity<LeaderboardDTO> {
        val timeFrameEnum = try {
            TimeFrame.valueOf(timeFrame.uppercase())
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.badRequest().build()
        }

        val categoryEnum = try {
            LeaderboardCategory.valueOf(category.uppercase())
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.badRequest().build()
        }

        return ResponseEntity.ok(leaderboardService.getOrCreateLeaderboard(timeFrameEnum, categoryEnum, eventCategoryId))
    }

    @PostMapping("/{leaderboardId}/update")
    fun updateLeaderboard(@PathVariable leaderboardId: Long): ResponseEntity<Void> {
        leaderboardService.updateLeaderboard(leaderboardId)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/update/all")
    fun updateAllLeaderboards(): ResponseEntity<Void> {
        leaderboardService.updateAllLeaderboards()
        return ResponseEntity.ok().build()
    }

    @GetMapping("/top/weekly")
    fun getTopUsersWeekly(
        @RequestParam(defaultValue = "10") limit: Int
    ): ResponseEntity<List<LeaderboardEntryDTO>> {
        return ResponseEntity.ok(leaderboardService.getTopUsersWeekly(limit))
    }

    @GetMapping("/top/monthly")
    fun getTopUsersMonthly(
        @RequestParam(defaultValue = "10") limit: Int
    ): ResponseEntity<List<LeaderboardEntryDTO>> {
        return ResponseEntity.ok(leaderboardService.getTopUsersMonthly(limit))
    }

    @GetMapping("/top/all-time")
    fun getTopUsersAllTime(
        @RequestParam(defaultValue = "10") limit: Int
    ): ResponseEntity<List<LeaderboardEntryDTO>> {
        return ResponseEntity.ok(leaderboardService.getTopUsersAllTime(limit))
    }
}