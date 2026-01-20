package ru.practicum.event;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.event.model.RequestAdminParams;
import ru.practicum.event.model.RequestPublicParams;
import ru.practicum.interaction.dto.event.*;
import ru.practicum.interaction.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.interaction.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.interaction.dto.request.RequestDto;

import java.util.List;
import java.util.Set;

public interface EventService {

    List<EventFullDto> getAllEvents(RequestAdminParams params);

    EventFullDto updateEvent(Long eventId, UpdateEventAdminRequest request);

    List<EventShortDto> getAllEvents(Long userId, Integer from, Integer size);

    EventFullDto createEvent(Long userId, NewEventDto newEventDto);

    EventFullDto getEvent(Long userId, Long eventId);

    EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest request);

    List<EventShortDto> getAllEvents(RequestPublicParams params, HttpServletRequest request);

    EventFullDto getEventById(Long eventId, Long userId, HttpServletRequest request);

    List<RequestDto> getEventParticipants(Long userId, Long eventId);

    EventRequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest req);

    Set<EventShortDto> findAllByIdIn(List<Long> eventIds);

    EventFullDto privateGetById(Long eventId);

    List<EventShortDto> getEventsByCategory(Long categoryId);

    List<EventShortDto> getRecommendations(Long userId);

    void like(Long eventId, Long userId);
}
