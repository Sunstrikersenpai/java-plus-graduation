package ru.practicum.collector;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "app.kafka")
@Getter
@Setter
public class KafkaProps {
    private String topic;
}