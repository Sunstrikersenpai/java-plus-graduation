package ru.practicum.requests.controller;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.interaction.dto.request.RequestDto;
import ru.practicum.interaction.enums.RequestStatus;
import ru.practicum.requests.service.RequestService;

import java.util.List;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
public class ClientRequestController {
    private final RequestService requestService;

    @GetMapping("/events/{eventId}")
    public List<RequestDto> getRequestsByEventId(@PathVariable @Positive Long eventId) {
        return requestService.getRequestsByEventId(eventId);
    }

    @GetMapping
    public List<RequestDto> getAllRequestsByIds(@RequestParam @NotNull List<Long> requestIds) {
        return requestService.getAllRequestsByIds(requestIds);
    }

    @GetMapping("events/{eventId}/status")
    public Long countRequests(
            @PathVariable @Positive Long eventId,
            @RequestParam @NotNull RequestStatus status
    ) {
        return requestService.countRequests(eventId, status);
    }

    @PostMapping
    public List<RequestDto> saveAll(@RequestBody List<RequestDto> requests) {
        return requestService.saveAll(requests);
    }

    @GetMapping("/status")
    List<RequestDto> getRequestsWithStatus(@RequestParam List<Long> requestId,
                                           @RequestParam RequestStatus status) {
        return requestService.getRequestsWithStatus(requestId, status);
    }

}
