package ru.practicum.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.analyzer.model.EventSimilarity;

import java.util.List;
import java.util.Optional;

public interface EventSimilarityRepository extends JpaRepository<EventSimilarity, Long> {

    @Query("""
                select e from EventSimilarity e
                where e.eventA = :eventId or e.eventB = :eventId
                order by e.score desc
            """)
    List<EventSimilarity> findSimilar(@Param("eventId") long eventId);

    Optional<EventSimilarity> findByEventAAndEventB(long eventA, long eventB);
}
