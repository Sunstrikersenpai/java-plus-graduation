package ru.practicum.client;

import com.google.protobuf.Empty;
import com.google.protobuf.Timestamp;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.proto.message.ActionTypeProto;
import ru.practicum.ewm.stats.proto.message.UserActionProto;
import ru.practicum.ewm.stats.proto.service.collector.UserActionControllerGrpc;

import java.time.Instant;

@Component
public class CollectorClient {

    @GrpcClient("collector")
    UserActionControllerGrpc.UserActionControllerBlockingStub client;

    public void collectAction(long userId, long eventId, String type, Instant timestamp) {
        UserActionProto req = UserActionProto.newBuilder()
                .setUserId(userId)
                .setEventId(eventId)
                .setActionType(ActionTypeProto.valueOf(type))
                .setTimestamp(Timestamp.newBuilder()
                        .setSeconds(timestamp.getEpochSecond())
                        .setNanos(timestamp.getNano())
                        .build())
                .build();

        try {
            Empty empty = client.collectUserAction(req);
        } catch (StatusRuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
