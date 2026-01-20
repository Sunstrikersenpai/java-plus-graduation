package ru.practicum.aggregator.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;


@Getter
@Setter
@AllArgsConstructor
@ConfigurationProperties(prefix = "app.kafka")
public class KafkaProps {
    private final Producer producer;
    private final Consumer consumer;
    private final String bootstrapServers;

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Producer {
        private String topic;
        private String keySerializer;
        private String valueSerializer;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Consumer {
        private String topic;
        private String valueDeserializer;
        private String keyDeserializer;
        private String autoOffsetReset;
        private String groupId;
        private long pollTimeout;
    }
}
