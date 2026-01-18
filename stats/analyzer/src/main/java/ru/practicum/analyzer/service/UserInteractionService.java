package ru.practicum.analyzer.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.analyzer.model.UserInteraction;
import ru.practicum.analyzer.model.Weights;
import ru.practicum.analyzer.repository.UserInteractionRepository;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@Service
@AllArgsConstructor
public class UserInteractionService {

    private final UserInteractionRepository repo;
    private final Weights weights;

    public void upsert(UserActionAvro action) {
        UserInteraction entity = repo.findByUserIdAndEventId(action.getUserId(), action.getEventId())
                .orElseGet(() -> {
                    UserInteraction e = new UserInteraction();
                    e.setUserId(action.getUserId());
                    e.setEventId(action.getEventId());
                    e.setWeight(0.0);
                    return e;
                });

        double newWeight = weights.getWeight(action.getActionType());
        entity.setWeight(Math.max(entity.getWeight(), newWeight));
        entity.setLastTimestamp(action.getTimestamp());

        repo.save(entity);
    }

}
