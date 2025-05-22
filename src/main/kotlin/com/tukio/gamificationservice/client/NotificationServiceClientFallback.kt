package com.tukio.gamificationservice.client

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import kotlin.jvm.java

@Component
class NotificationServiceClientFallback : NotificationServiceClient {

    private val logger = LoggerFactory.getLogger(NotificationServiceClientFallback::class.java)

    override fun sendNotification(request: NotificationRequestDTO): ResponseEntity<List<NotificationResponseDTO>> {
        logger.warn("Fallback: sendNotification for user ${request.userId}")
        return ResponseEntity.ok(emptyList())
    }

}
