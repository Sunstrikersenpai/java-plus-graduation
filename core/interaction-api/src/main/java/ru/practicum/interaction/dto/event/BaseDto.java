package ru.practicum.interaction.dto.event;

public interface BaseDto {
    void setViews(Long views);

    void setConfirmedRequests(Long confirmedRequests);

    Long getId();
}
