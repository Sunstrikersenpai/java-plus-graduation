package ru.practicum.analyzer.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.analyzer.model.EventSimilarity;
import ru.practicum.analyzer.repository.EventSimilarityRepository;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

@Service
@AllArgsConstructor
public class SimilarityService {

    private final EventSimilarityRepository repo;

    public void upsert(EventSimilarityAvro avro) {
        EventSimilarity entity = repo.findByEventAAndEventB(avro.getEventA(), avro.getEventB())
                .map(e -> {
                    e.setScore(avro.getScore());
                    e.setTimestamp(avro.getTimestamp());
                    return repo.save(e);
                })
                .orElseGet(() -> {
                    EventSimilarity e = new EventSimilarity();
                    e.setScore(avro.getScore());
                    e.setTimestamp(e.getTimestamp());
                    e.setEventA(avro.getEventA());
                    e.setEventB(e.getEventB());
                    return repo.save(e);
                });
    }
}
