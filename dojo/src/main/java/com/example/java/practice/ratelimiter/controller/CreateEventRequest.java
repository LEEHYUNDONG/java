package com.example.java.practice.ratelimiter.controller;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateEventRequest(
        UUID eventId,
        String eventType,
        Long userId,
        LocalDateTime eventTime
) {
}
