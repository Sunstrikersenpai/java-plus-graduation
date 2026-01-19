package ru.practicum.analyzer.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;

import java.util.EnumMap;
import java.util.Map;

@ConfigurationProperties(prefix = "app.action.weight")
@Getter
@Setter
public class Weights {
    private double view;
    private double registered;
    private double like;

    public double getWeight(ActionTypeAvro type) {
        return switch (type) {
            case VIEW -> view;
            case REGISTER -> registered;
            case LIKE -> like;
        };
    }
}
