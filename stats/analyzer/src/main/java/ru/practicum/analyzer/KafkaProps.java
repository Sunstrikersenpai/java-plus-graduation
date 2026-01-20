package ru.practicum.analyzer;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.kafka")
@Component
public class KafkaProps {

    private long pollTimeout;
    private ConsumerProps actionConsumer = new ConsumerProps();
    private ConsumerProps similarityConsumer = new ConsumerProps();

    @Getter
    @Setter
    public static class ConsumerProps {
        private String topic;
        private Map<String, Object> properties = new HashMap<>();
    }
}
