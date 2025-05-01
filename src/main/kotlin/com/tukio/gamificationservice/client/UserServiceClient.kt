package com.tukio.gamificationservice.client

import com.tukio.gamificationservice.dto.UserDTO
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(name = "tukio-user-service", fallback = UserServiceClientFallback::class)
interface UserServiceClient {

    @GetMapping("/api/users/{id}")
    fun getUserById(@PathVariable("id") userId: Long): UserDTO

    @GetMapping("/api/users/profile/{id}")
    fun getUserProfile(@PathVariable("id") userId: Long): UserDTO

    @GetMapping("/api/users")
    fun getUsersByIds(@RequestParam("ids") userIds: List<Long>): List<UserDTO>
}