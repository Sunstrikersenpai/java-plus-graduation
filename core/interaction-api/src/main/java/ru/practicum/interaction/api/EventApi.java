package ru.practicum.interaction.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.interaction.dto.event.EventFullDto;
import ru.practicum.interaction.dto.event.EventShortDto;

import java.util.List;
import java.util.Set;

public interface EventApi {

    @GetMapping("/admin/events/{eventId}")
    EventFullDto privateGetById(@PathVariable Long eventId);

    @GetMapping("/events/idIn")
    Set<EventShortDto> findAllByIdIn(@RequestParam List<Long> eventIds);

    @GetMapping("/admin/events/category/{categoryId}")
    List<EventShortDto> getEventsByCategory(@PathVariable Long categoryId);
}
