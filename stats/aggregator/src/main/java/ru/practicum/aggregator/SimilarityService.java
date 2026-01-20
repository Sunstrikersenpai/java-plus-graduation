package ru.practicum.aggregator;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;
import java.util.*;

@Component
@AllArgsConstructor
@Slf4j
public class SimilarityService {
    private final Weights weights;
    private final Map<Long, Map<Long, Double>> eventUserWeights = new HashMap<>();
    private final Map<Long, Double> eventTotalWeights = new HashMap<>();
    private final Map<Long, Map<Long, Double>> minWeightsSums = new HashMap<>();

    public List<EventSimilarityAvro> process(UserActionAvro action) {
        long userId = action.getUserId();
        long eventId = action.getEventId();
        ActionTypeAvro actionType = action.getActionType();

        double newWeightCandidate = weights.getWeight(actionType);

        Map<Long, Double> userWeightsForEvent =
                eventUserWeights.computeIfAbsent(eventId, k -> new HashMap<>());

        double oldWeight = userWeightsForEvent.getOrDefault(userId, 0.0);
        double newWeight = Math.max(oldWeight, newWeightCandidate);

        if (oldWeight >= newWeight) {
            return Collections.emptyList();
        }

        userWeightsForEvent.put(userId, newWeight);
        updateEventTotalWeights(eventId, oldWeight, newWeight);

        List<EventSimilarityAvro> out = new ArrayList<>();

        for (Map.Entry<Long, Map<Long, Double>> entry : eventUserWeights.entrySet()) {
            long otherEventId = entry.getKey();
            if (otherEventId == eventId) {
                continue;
            }
            Map<Long, Double> otherUsers = entry.getValue();
            Double otherWeightObj = otherUsers.get(userId);
            if (otherWeightObj == null) {
                continue;
            }
            double otherWeight = otherUsers.get(userId);

            long a = Math.min(eventId, otherEventId);
            long b = Math.max(eventId, otherEventId);

            double oldMin = Math.min(oldWeight, otherWeight);
            double newMin = Math.min(newWeight, otherWeight);

            double minSum;
            if (oldMin != newMin) {
                minSum = updateMinSum(a, b, oldMin, newMin);
            } else {
                minSum = minWeightsSums.getOrDefault(a, Collections.emptyMap()).getOrDefault(b, 0.0);
            }

            double sumA = eventTotalWeights.getOrDefault(a, 0.0);
            double sumB = eventTotalWeights.getOrDefault(b, 0.0);
            double sim = (sumA == 0.0 || sumB == 0.0) ? 0.0 : minSum / Math.sqrt(sumA * sumB);

            out.add(EventSimilarityAvro.newBuilder()
                    .setEventA(a)
                    .setEventB(b)
                    .setScore(sim)
                    .setTimestamp(Instant.now())
                    .build());
        }

        return out;
    }


    private void updateEventTotalWeights(long eventId, double oldW, double newW) {
        double sum = eventTotalWeights.getOrDefault(eventId, 0.0);
        sum = sum - oldW + newW;
        eventTotalWeights.put(eventId, sum);
    }

    private double updateMinSum(long a, long b, double oldMin, double newMin) {
        Map<Long, Double> row = minWeightsSums.computeIfAbsent(a, k -> new HashMap<>());
        double current = row.getOrDefault(b, 0.0);
        double updated = current - oldMin + newMin;
        row.put(b, updated);
        return updated;
    }
}
