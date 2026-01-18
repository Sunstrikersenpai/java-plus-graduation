package ru.practicum.aggregator;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;
import java.util.*;

@Component
@AllArgsConstructor
public class SimilarityService {
    private final Weights weights;
    private final Map<Long, Map<Long, Double>> eventUserWeights = new HashMap<>();
    private final Map<Long, Double> eventTotalWeights = new HashMap<>();
    private final Map<Long, Map<Long, Double>> minWeightsSums = new HashMap<>();

    public List<EventSimilarityAvro> process(UserActionAvro action) {
        long userId = action.getUserId();
        long eventId = action.getEventId();

        double newWeightCandidate = weights.getWeight(action.getActionType());

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
            if (otherEventId == eventId) continue;

            Map<Long, Double> otherUsers = entry.getValue();
            double otherWeight = otherUsers.getOrDefault(userId, 0.0);

            long a = Math.min(eventId, otherEventId);
            long b = Math.max(eventId, otherEventId);

            double oldMin = Math.min(oldWeight, otherWeight);
            double newMin = Math.min(newWeight, otherWeight);

            if (oldMin != newMin) {
                double updatedMinSum = updateMinSum(a, b, oldMin, newMin);

                double sim = calculateSimilarity(a, b, updatedMinSum);
                EventSimilarityAvro msg = EventSimilarityAvro.newBuilder()
                        .setEventA(a)
                        .setEventB(b)
                        .setScore(sim)
                        .setTimestamp(Instant.now())
                        .build();
                out.add(msg);
            }
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

    private double calculateSimilarity(long a, long b, double minSum) {
        double sumA = eventTotalWeights.getOrDefault(a, 0.0);
        double sumB = eventTotalWeights.getOrDefault(b, 0.0);
        if (sumA == 0.0 || sumB == 0.0) return 0.0;

        return minSum / Math.sqrt(sumA * sumB);
    }
}
