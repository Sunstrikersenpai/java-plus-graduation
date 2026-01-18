package ru.practicum.event;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.practicum.event.model.Event;
import ru.practicum.interaction.enums.State;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {
    Page<Event> findAllByInitiatorIdOrderByCreatedOnDesc(Long initiator, Pageable pageable);

    Optional<Event> findEventByIdAndState(Long id, State state);

    List<Event> findEventsByCategory(Long categoryId);
}
