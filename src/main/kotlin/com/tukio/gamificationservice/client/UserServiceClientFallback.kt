package com.tukio.gamificationservice.client

import com.tukio.gamificationservice.dto.UserDTO
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class UserServiceClientFallback : UserServiceClient {

    private val logger = LoggerFactory.getLogger(UserServiceClientFallback::class.java)

    override fun getUserById(userId: Long): UserDTO {
        logger.warn("Fallback: getUserById for userId $userId")
        return createEmptyUserDTO(userId)
    }

    override fun getUserProfile(userId: Long): UserDTO {
        logger.warn("Fallback: getUserProfile for userId $userId")
        return createEmptyUserDTO(userId)
    }

    override fun getUsersByIds(userIds: List<Long>): List<UserDTO> {
        logger.warn("Fallback: getUsersByIds for userIds $userIds")
        return userIds.map { createEmptyUserDTO(it) }
    }

    private fun createEmptyUserDTO(userId: Long): UserDTO {
        return UserDTO(
            id = userId,
            username = "unknown_user",
            firstName = "Unknown",
            lastName = "User",
            profilePictureUrl = null
        )
    }
}