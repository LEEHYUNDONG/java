package com.example.java.practice.ratelimiter.infrastructure.persistence;


import com.example.java.practice.ratelimiter.domain.Event;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter(AccessLevel.PRIVATE) @Getter(AccessLevel.PUBLIC)
@Entity
@Table(name="events")
public class EventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private UUID eventId;

    private String eventType;

    private Long userId;

    private LocalDateTime eventTime;

    public static EventEntity of(Event event) {
        EventEntity eventEntity = new EventEntity();
        eventEntity.setEventId(event.getEventId());
        eventEntity.setEventType(event.getEventType());
        eventEntity.setUserId(event.getUserId());
        eventEntity.setEventTime(event.getEventTime());

        return eventEntity;

    }
}
