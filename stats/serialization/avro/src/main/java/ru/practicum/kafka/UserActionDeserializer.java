package ru.practicum.kafka;

import ru.practicum.ewm.stats.avro.UserActionAvro;

public class UserActionDeserializer extends GenericAvroDeserializer<UserActionAvro> {
    public UserActionDeserializer() {
        super(UserActionAvro.getClassSchema());
    }
}
