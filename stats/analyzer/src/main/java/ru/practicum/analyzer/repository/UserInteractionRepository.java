package ru.practicum.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.analyzer.model.UserInteraction;

import java.util.List;
import java.util.Optional;

public interface UserInteractionRepository
        extends JpaRepository<UserInteraction, Long> {

    List<UserInteraction> findByUserId(long userId);

    List<UserInteraction> findByEventIdIn(List<Long> eventIds);

    Optional<UserInteraction> findByUserIdAndEventId(long userId, long eventId);
}
