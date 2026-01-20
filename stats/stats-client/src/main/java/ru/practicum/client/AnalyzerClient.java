package ru.practicum.client;

import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.proto.message.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.proto.message.RecommendedEventProto;
import ru.practicum.ewm.stats.proto.message.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.proto.message.UserPredictionsRequestProto;
import ru.practicum.ewm.stats.proto.service.dashboard.RecommendationsControllerGrpc;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Component
public class AnalyzerClient {

    @GrpcClient("analyzer")
    RecommendationsControllerGrpc.RecommendationsControllerBlockingStub client;

    private static Stream<RecommendedEventProto> stream(Iterator<RecommendedEventProto> iterator) {
        var spliterator = Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED);
        return StreamSupport.stream(spliterator, false);
    }

    public Stream<RecommendedEventProto> getRecommendationsForUser(long userId, int maxResults) {
        UserPredictionsRequestProto req = UserPredictionsRequestProto.newBuilder()
                .setUserId(userId)
                .setMaxResults(maxResults)
                .build();

        try {
            Iterator<RecommendedEventProto> it = client.getRecommendationsForUser(req);
            return stream(it);
        } catch (StatusRuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Stream<RecommendedEventProto> getSimilarEvents(long eventId, long userId, int maxResults) {
        SimilarEventsRequestProto req = SimilarEventsRequestProto.newBuilder()
                .setEventId(eventId)
                .setUserId(userId)
                .setMaxResults(maxResults)
                .build();

        try {
            Iterator<RecommendedEventProto> it = client.getSimilarEvents(req);
            return stream(it);
        } catch (StatusRuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Stream<RecommendedEventProto> getInteractionsCount(List<Long> eventIds) {
        InteractionsCountRequestProto req = InteractionsCountRequestProto.newBuilder()
                .addAllEventId(eventIds)
                .build();

        try {
            Iterator<RecommendedEventProto> it = client.getInteractionsCount(req);
            return stream(it);
        } catch (StatusRuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
