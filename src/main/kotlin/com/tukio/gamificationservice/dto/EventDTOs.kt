package com.tukio.gamificationservice.dto

import java.time.LocalDateTime

data class EventDTO(
    val id: Long,
    val title: String,
    val categoryId: Long,
    val categoryName: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime
)

data class EventCategoryDTO(
    val id: Long,
    val name: String,
    val description: String?,
    val color: String?
)