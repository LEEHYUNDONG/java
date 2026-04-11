package com.example.java.practice.ratelimiter.controller;


import com.example.java.practice.ratelimiter.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class EventController {

    private final EventService eventService;


    @PostMapping("/api/v1/event")
    public ResponseEntity<Void> createEvent(@RequestBody CreateEventRequest request) {
        eventService.createEvent(request);

        return ResponseEntity.accepted().build();
    }

}
