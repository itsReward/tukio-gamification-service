package com.tukio.gamificationservice.dto

data class UserDTO(
    val id: Long,
    val username: String,
    val firstName: String,
    val lastName: String,
    val profilePictureUrl: String?
)