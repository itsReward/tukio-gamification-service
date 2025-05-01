package com.tukio.gamificationservice.service

import com.tukio.gamificationservice.dto.PointTransactionDTO
import com.tukio.gamificationservice.dto.PointTransactionResponseDTO
import com.tukio.gamificationservice.dto.UserPointsDTO
import com.tukio.gamificationservice.model.ActivityType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface PointService {

    fun getUserPoints(userId: Long): UserPointsDTO

    fun addPoints(transactionDTO: PointTransactionDTO): PointTransactionResponseDTO

    fun getUserTransactions(userId: Long, pageable: Pageable): Page<PointTransactionResponseDTO>

    fun getUserTransactionsByActivityType(userId: Long, activityType: ActivityType): List<PointTransactionResponseDTO>

    fun calculateLevelForPoints(points: Int): Int

    fun getPointsRequiredForNextLevel(currentLevel: Int): Int

    fun resetWeeklyPoints()

    fun resetMonthlyPoints()
}