package com.example.java.practice.ratelimiter.domain;


import com.example.java.practice.ratelimiter.controller.CreateEventRequest;

import java.time.LocalDateTime;
import java.util.UUID;


public class Event {

    private UUID eventId;

    private String eventType;

    private Long userId;

    private LocalDateTime eventTime;

    public static Event createEvent(CreateEventRequest request) {
        return new Event(request.eventId(),
                request.eventType(),
                request.userId(),
                request.eventTime());
    }


    public Event(UUID eventId, String eventType, Long userId, LocalDateTime eventTime) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.userId = userId;
        this.eventTime = eventTime;
    }

    public UUID getEventId() {
        return eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public Long getUserId() {
        return userId;
    }

    public LocalDateTime getEventTime() {
        return eventTime;
    }
}
