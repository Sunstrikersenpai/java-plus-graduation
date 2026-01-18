package ru.practicum.collector;

import com.google.protobuf.Timestamp;
import lombok.AllArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.proto.message.ActionTypeProto;
import ru.practicum.ewm.stats.proto.message.UserActionProto;

import java.time.Instant;

@AllArgsConstructor
@Component
public class UserActionHandler {
    private final KafkaProps kafkaProps;
    private final KafkaTemplate<Long, UserActionAvro> kafkaTemplate;

    public void handle(UserActionProto userActionProto) {
        UserActionAvro userActionAvro = UserActionAvro.newBuilder()
                .setUserId(userActionProto.getUserId())
                .setEventId(userActionProto.getEventId())
                .setActionType(mapAction(userActionProto.getActionType()))
                .setTimestamp(toInstant(userActionProto.getTimestamp()))
                .build();

        kafkaTemplate.send(kafkaProps.getTopic(), userActionProto.getUserId(), userActionAvro);
    }

    private ActionTypeAvro mapAction(ActionTypeProto proto) {
        return switch (proto) {
            case ACTION_VIEW -> ActionTypeAvro.VIEW;
            case ACTION_REGISTER -> ActionTypeAvro.REGISTER;
            case ACTION_LIKE -> ActionTypeAvro.LIKE;
            default -> throw new IllegalArgumentException("Unsupported action_type:" + proto);
        };
    }

    private Instant toInstant(Timestamp t) {
        return Instant.ofEpochSecond(t.getSeconds(), t.getNanos());
    }
}
