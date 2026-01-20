package ru.practicum.analyzer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;
import ru.practicum.analyzer.service.SimilarityService;
import ru.practicum.analyzer.service.UserInteractionService;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Slf4j
public class RecommendationsLoop implements SmartLifecycle {

    private final Consumer<String, UserActionAvro> userActionConsumer;
    private final Consumer<String, EventSimilarityAvro> similarityConsumer;
    private final UserInteractionService userInteractionService;
    private final SimilarityService similarityService;
    private final KafkaProps props;

    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    private volatile boolean running = false;

    public RecommendationsLoop(KafkaProps props, UserInteractionService uis, SimilarityService ss) {
        this.props = props;
        this.userActionConsumer = new KafkaConsumer<>(props.getActionConsumer().getProperties());
        this.similarityConsumer = new KafkaConsumer<>(props.getSimilarityConsumer().getProperties());
        this.userInteractionService = uis;
        this.similarityService = ss;
    }

    @Override
    public void start() {
        running = true;

        userActionConsumer.subscribe(List.of(props.getActionConsumer().getTopic()));
        similarityConsumer.subscribe(List.of(props.getSimilarityConsumer().getTopic()));

        executor.submit(this::userActionLoop);
        executor.submit(this::similarityLoop);
    }

    private void userActionLoop() {
        try {
            while (running) {
                var records = userActionConsumer.poll(Duration.ofMillis(500));
                for (var record : records) {
                    userInteractionService.upsert(record.value());
                }
                userActionConsumer.commitAsync();
            }
        } catch (WakeupException ignored) {
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            try {
                userActionConsumer.commitSync();
            } finally {
                userActionConsumer.close();
            }
        }
    }

    private void similarityLoop() {
        try {
            while (running) {
                var records = similarityConsumer.poll(Duration.ofMillis(500));
                for (var record : records) {
                    similarityService.upsert(record.value());
                }
                similarityConsumer.commitAsync();
            }
        } catch (WakeupException ignored) {
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            try {
                similarityConsumer.commitSync();
            } finally {
                similarityConsumer.close();
            }
        }
    }


    @Override
    public void stop() {
        running = false;
        userActionConsumer.wakeup();
        similarityConsumer.wakeup();
        executor.shutdown();
    }

    @Override
    public boolean isRunning() {
        return running;
    }

}
