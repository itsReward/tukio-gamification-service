package com.tukio.gamificationservice.model

import jakarta.persistence.Enumerated
import jakarta.persistence.EnumType

enum class ActivityType {
    EVENT_REGISTRATION,      // Registering for an event
    EVENT_ATTENDANCE,        // Attending an event
    EVENT_RATING,            // Rating an event
    EVENT_SHARING,           // Sharing an event
    BADGE_EARNED,            // Earning a badge
    LEVEL_UP,                // Leveling up
    CONSECUTIVE_ATTENDANCE,  // Attending events consecutively
    DIVERSE_CATEGORIES,      // Attending diverse event categories
    REFERRAL,                // Referring another user
    BONUS,                   // Bonus points (admin awarded)
    POINT_ADJUSTMENT         // Administrative adjustment
}