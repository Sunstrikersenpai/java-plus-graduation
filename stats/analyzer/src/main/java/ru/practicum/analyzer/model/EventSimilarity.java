package ru.practicum.analyzer.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "event_similarity")
@Getter
@Setter
public class EventSimilarity {
    @Id
    private Long id;
    private Long eventA;
    private Long eventB;
    private double score;
    private Instant timestamp;
}