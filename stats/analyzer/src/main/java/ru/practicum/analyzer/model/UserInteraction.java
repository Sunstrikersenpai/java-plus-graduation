package ru.practicum.analyzer.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "user_event_interaction")
@Getter
@Setter
public class UserInteraction {
    @Id
    private Long id;
    private Long userId;
    private Long eventId;
    private double weight;
    private Instant lastTimestamp;
}
