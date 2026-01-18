package ru.practicum.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.EventService;
import ru.practicum.event.model.RequestPublicParams;
import ru.practicum.interaction.dto.event.EventFullDto;
import ru.practicum.interaction.dto.event.EventShortDto;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Slf4j
public class PublicEventController {

    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> getEvents(
            @ModelAttribute RequestPublicParams params,
            HttpServletRequest request
    ) {
        return eventService.getAllEvents(params, request);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEventById(
            @PathVariable Long eventId,
            @RequestHeader("X-EWM-USER-ID") Long userId,
            HttpServletRequest request
    ) {
        return eventService.getEventById(eventId, userId, request);
    }

    @GetMapping("/idIn")
    public Set<EventShortDto> findAllByIdIn(@RequestParam List<Long> eventIds) {
        return eventService.findAllByIdIn(eventIds);
    }

    @GetMapping("/recommendation")
    List<EventShortDto> getRecommendations(@RequestHeader("X-EWM-USER-ID") Long userId) {
        return eventService.getRecommendations(userId);
    }

    @PutMapping("/{eventId}/like")
    void like(@PathVariable Long eventId,
              @RequestHeader("X-EWM-USER-ID") Long userId) {
        eventService.like(eventId, userId);
    }
}
