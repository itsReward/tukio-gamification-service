package com.tukio.gamificationservice.client

import com.tukio.gamificationservice.dto.EventDTO
import com.tukio.gamificationservice.dto.EventCategoryDTO
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@FeignClient(name = "tukio-events-service", fallback = EventServiceClientFallback::class)
interface EventServiceClient {

    @GetMapping("/api/events/{id}")
    fun getEventById(@PathVariable("id") eventId: Long): EventDTO

    @GetMapping("/api/event-categories/{id}")
    fun getEventCategoryById(@PathVariable("id") categoryId: Long): EventCategoryDTO
}