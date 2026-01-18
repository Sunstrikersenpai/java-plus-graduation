package ru.practicum.aggregator;

import org.springframework.boot.context.properties.ConfigurationProperties;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;

import java.util.EnumMap;
import java.util.Map;

@ConfigurationProperties(prefix = "app.action.weight")
public class Weights {
    private final Map<ActionTypeAvro, Double> weights = new EnumMap<>(ActionTypeAvro.class);
    private Double view;
    private Double registered;
    private Double like;

    public Weights() {
        weights.put(ActionTypeAvro.VIEW, view);
        weights.put(ActionTypeAvro.REGISTER, registered);
        weights.put(ActionTypeAvro.LIKE, like);
    }

    public double getWeight(ActionTypeAvro type) {
        return weights.get(type);
    }
}
