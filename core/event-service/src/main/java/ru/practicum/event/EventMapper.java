package ru.practicum.event;


import ru.practicum.event.model.Event;
import ru.practicum.interaction.dto.category.CategoryDto;
import ru.practicum.interaction.dto.event.EventFullDto;
import ru.practicum.interaction.dto.event.EventShortDto;
import ru.practicum.interaction.dto.event.NewEventDto;
import ru.practicum.interaction.dto.user.UserDto;
import ru.practicum.interaction.dto.user.UserShortDto;

import java.util.Map;

public class EventMapper {
    public static EventShortDto toEventShortDto(Event event, Map<Long, CategoryDto> catMap, Map<Long, UserShortDto> userMap) {
        return EventShortDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .eventDate(event.getEventDate())
                .paid(event.getPaid())
                .title(event.getTitle())
                .category(catMap.get(event.getCategory()))
                .initiator(userMap.get(event.getInitiatorId()))
                .build();
    }

    public static EventFullDto toEventFullDto(Event event,Map<Long, CategoryDto> catMap, Map<Long, UserShortDto> userMap) {
        return EventFullDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .annotation(event.getAnnotation())
                .category(catMap.get(event.getCategory()))
                .paid(event.getPaid())
                .eventDate(event.getEventDate())
                .initiator(userMap.get(event.getInitiatorId()))
                .description(event.getDescription())
                .participantLimit(event.getParticipantLimit())
                .state(event.getState())
                .createdOn(event.getCreatedOn())
                .publishedOn(event.getPublishedOn())
                .location(event.getLocation())
                .requestModeration(event.getRequestModeration())
                .views(0L)
                .confirmedRequests(0L)
                .build();
    }

    public static EventFullDto toEventFullDto(Event event,CategoryDto categoryDto, UserShortDto userDto) {
        return EventFullDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .annotation(event.getAnnotation())
                .category(categoryDto)
                .paid(event.getPaid())
                .eventDate(event.getEventDate())
                .initiator(userDto)
                .description(event.getDescription())
                .participantLimit(event.getParticipantLimit())
                .state(event.getState())
                .createdOn(event.getCreatedOn())
                .publishedOn(event.getPublishedOn())
                .location(event.getLocation())
                .requestModeration(event.getRequestModeration())
                .views(0L)
                .confirmedRequests(0L)
                .build();
    }

    public static Event toEvent(NewEventDto newEventDto) {
        return Event.builder()
                .annotation(newEventDto.getAnnotation())
                .description(newEventDto.getDescription())
                .eventDate(newEventDto.getEventDate())
                .location(newEventDto.getLocation())
                .paid(newEventDto.getPaid() != null ? newEventDto.getPaid() : false)
                .participantLimit(newEventDto.getParticipantLimit() != null ? newEventDto.getParticipantLimit() : 0)
                .requestModeration(newEventDto.getRequestModeration() != null ? newEventDto.getRequestModeration() : true)
                .title(newEventDto.getTitle())
                .build();
    }
}
