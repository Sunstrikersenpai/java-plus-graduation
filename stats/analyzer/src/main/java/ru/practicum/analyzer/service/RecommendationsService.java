package ru.practicum.analyzer.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.analyzer.model.UserInteraction;
import ru.practicum.analyzer.repository.EventSimilarityRepository;
import ru.practicum.analyzer.repository.UserInteractionRepository;
import ru.practicum.ewm.stats.proto.message.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.proto.message.RecommendedEventProto;
import ru.practicum.ewm.stats.proto.message.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.proto.message.UserPredictionsRequestProto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@AllArgsConstructor
@Service
public class RecommendationsService {
    private final UserInteractionRepository interactionRepo;
    private final EventSimilarityRepository similarityRepo;

    public Stream<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto request) {
        long eventId = request.getEventId();
        int limit = request.getMaxResults();

        if (limit <= 0) {
            return Stream.empty();
        }

        Set<Long> interacted = interactionRepo.findByUserId(request.getUserId()).stream()
                .map(UserInteraction::getEventId)
                .collect(Collectors.toSet());

        return similarityRepo.findSimilar(eventId).stream()
                .map(es -> {
                    long candidate = (es.getEventA() == eventId) ? es.getEventB() : es.getEventA();
                    return Map.entry(candidate, es.getScore());
                })
                .filter(e -> e.getKey() != eventId)
                .filter(e -> !interacted.contains(e.getKey()))
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(limit)
                .map(e -> toProto(e.getKey(), e.getValue()));

    }

    public Stream<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto request) {
        if (request.getEventIdList().isEmpty()) {
            return Stream.empty();
        }

        Map<Long, Double> eventScoreMap = request.getEventIdList().stream()
                .collect(Collectors.toMap(
                        id -> id,
                        score -> 0.0
                ));

        interactionRepo.findByEventIdIn(request.getEventIdList())
                .forEach(interaction -> {
                    double weight = interaction.getWeight();
                    eventScoreMap.merge(interaction.getEventId(), weight, Double::sum);
                });

        return eventScoreMap.entrySet().stream()
                .map(e -> toProto(e.getKey(), e.getValue()));
    }

    public Stream<RecommendedEventProto> getRecommendationsForUser(UserPredictionsRequestProto request) {
        long userId = request.getUserId();
        int limit = request.getMaxResults();
        if (limit <= 0) return Stream.empty();

        List<UserInteraction> interactions = interactionRepo.findByUserId(userId);
        if (interactions.isEmpty()) return Stream.empty();

        Set<Long> interacted = interactions.stream()
                .map(UserInteraction::getEventId)
                .collect(Collectors.toSet());

        Map<Long, Double> candidateScores = new HashMap<>();

        for (UserInteraction base : interactions) {
            long baseEventId = base.getEventId();
            double baseWeight = base.getWeight();

            similarityRepo.findSimilar(baseEventId).forEach(sim -> {
                long candidate = (sim.getEventA() == baseEventId) ? sim.getEventB() : sim.getEventA();
                if (candidate == baseEventId) return;
                if (interacted.contains(candidate)) return;

                candidateScores.merge(candidate, baseWeight * sim.getScore(), Double::sum);
            });
        }

        return candidateScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(limit)
                .map(e -> toProto(e.getKey(), e.getValue()));
    }

    private RecommendedEventProto toProto(Long eventId, Double score) {
        return RecommendedEventProto.newBuilder()
                .setEventId(eventId)
                .setScore(score != null ? score : 0.0)
                .build();
    }
}
