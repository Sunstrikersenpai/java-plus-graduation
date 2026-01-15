package ru.practicum.interaction.api;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.*;
import ru.practicum.interaction.dto.request.RequestDto;
import ru.practicum.interaction.enums.RequestStatus;

import java.util.List;

public interface RequestApi {
    @GetMapping("/requests/events/{eventId}")
    List<RequestDto> getRequestsByEventId(@PathVariable @Positive Long eventId);

    @GetMapping("/requests")
    List<RequestDto> getAllRequestsByIds(@RequestParam @NotNull List<Long> requestIds);

    @GetMapping("/requests/events/{eventId}/status")
    Long countRequests(@PathVariable @Positive Long eventId, @RequestParam @NotNull RequestStatus status);

    @PostMapping("/requests")
    List<RequestDto> saveAll(@RequestBody List<RequestDto> requests);

    @GetMapping("/requests/status")
    List<RequestDto> getRequestsWithStatus(@RequestParam @NotNull List<Long> requestId,
                                           @RequestParam @NotNull RequestStatus status);
}
