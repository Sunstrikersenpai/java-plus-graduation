package ru.practicum.analyzer.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "event_similarity")
@Getter
@Setter
public class EventSimilarity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "event_a", nullable = false)
    private Long eventA;
    @Column(name = "event_b", nullable = false)
    private Long eventB;
    private double score;
    private Instant timestamp;
}