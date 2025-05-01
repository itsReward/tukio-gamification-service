package com.tukio.gamificationservice.exception

import java.time.LocalDateTime

class ResourceNotFoundException(message: String) : RuntimeException(message)

class InvalidRequestException(message: String) : RuntimeException(message)

class ServiceUnavailableException(message: String) : RuntimeException(message)

data class ErrorResponse(
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val status: Int,
    val error: String,
    val message: String,
    val path: String
)