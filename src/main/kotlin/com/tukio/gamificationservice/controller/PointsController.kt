package com.tukio.gamificationservice.controller

import com.tukio.gamificationservice.dto.PointTransactionDTO
import com.tukio.gamificationservice.dto.PointTransactionResponseDTO
import com.tukio.gamificationservice.dto.UserPointsDTO
import com.tukio.gamificationservice.model.ActivityType
import com.tukio.gamificationservice.service.PointService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/points")
class PointsController(private val pointService: PointService) {

    @GetMapping("/users/{userId}")
    fun getUserPoints(@PathVariable userId: Long): ResponseEntity<UserPointsDTO> {
        return ResponseEntity.ok(pointService.getUserPoints(userId))
    }

    @PostMapping("/add")
    fun addPoints(@RequestBody transaction: PointTransactionDTO): ResponseEntity<PointTransactionResponseDTO> {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(pointService.addPoints(transaction))
    }

    @GetMapping("/users/{userId}/transactions")
    fun getUserTransactions(
        @PathVariable userId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "timestamp") sortBy: String,
        @RequestParam(defaultValue = "DESC") direction: String
    ): ResponseEntity<Page<PointTransactionResponseDTO>> {
        val sortDirection = if (direction.uppercase() == "ASC") Sort.Direction.ASC else Sort.Direction.DESC
        val pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy))

        return ResponseEntity.ok(pointService.getUserTransactions(userId, pageable))
    }

    @GetMapping("/users/{userId}/activities/{activityType}")
    fun getUserTransactionsByActivityType(
        @PathVariable userId: Long,
        @PathVariable activityType: String
    ): ResponseEntity<List<PointTransactionResponseDTO>> {
        val activity = try {
            ActivityType.valueOf(activityType.uppercase())
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.badRequest().build()
        }

        return ResponseEntity.ok(pointService.getUserTransactionsByActivityType(userId, activity))
    }

    @GetMapping("/level")
    fun getLevelInfo(
        @RequestParam points: Int
    ): ResponseEntity<Map<String, Any>> {
        val level = pointService.calculateLevelForPoints(points)
        val nextLevelPoints = pointService.getPointsRequiredForNextLevel(level)

        val response = mapOf(
            "currentLevel" to level,
            "pointsForNextLevel" to nextLevelPoints,
            "pointsNeeded" to (nextLevelPoints - points)
        )

        return ResponseEntity.ok(response)
    }
}