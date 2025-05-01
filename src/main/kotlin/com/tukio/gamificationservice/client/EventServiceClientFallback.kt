package com.tukio.gamificationservice.client

import com.tukio.gamificationservice.dto.EventCategoryDTO
import com.tukio.gamificationservice.dto.EventDTO
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class EventServiceClientFallback : EventServiceClient {

    private val logger = LoggerFactory.getLogger(EventServiceClientFallback::class.java)

    override fun getEventById(eventId: Long): EventDTO {
        logger.warn("Fallback: getEventById for eventId $eventId")
        return EventDTO(
            id = eventId,
            title = "Unknown Event",
            categoryId = 0,
            categoryName = "Unknown",
            startTime = LocalDateTime.now(),
            endTime = LocalDateTime.now().plusHours(1)
        )
    }

    override fun getEventCategoryById(categoryId: Long): EventCategoryDTO {
        logger.warn("Fallback: getEventCategoryById for categoryId $categoryId")
        return EventCategoryDTO(
            id = categoryId,
            name = "Unknown Category",
            description = null,
            color = null
        )
    }
}