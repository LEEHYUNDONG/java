package com.example.java.practice.ratelimiter.service;


import com.example.java.practice.ratelimiter.controller.CreateEventRequest;
import com.example.java.practice.ratelimiter.domain.Event;
import com.example.java.practice.ratelimiter.infrastructure.persistence.EventEntity;
import com.example.java.practice.ratelimiter.infrastructure.persistence.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class EventService {

    private final EventRepository eventRepository;

    public Event createEvent(CreateEventRequest request) {
        Event event = Event.createEvent(request);
        eventRepository.save(EventEntity.of(event));

        return event;
    }
}
