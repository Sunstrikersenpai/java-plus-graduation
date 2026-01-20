package ru.practicum.aggregator;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;
import ru.practicum.aggregator.kafka.KafkaProps;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Slf4j
public class AggregationLoop implements SmartLifecycle {

    private final Consumer<String, SpecificRecordBase> consumer;
    private final Producer<String, SpecificRecordBase> producer;
    private final KafkaProps props;
    private final SimilarityService similarityService;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private volatile boolean running = false;

    public AggregationLoop(KafkaConsumer<String, SpecificRecordBase> consumer,
                           KafkaProducer<String, SpecificRecordBase> producer,
                           SimilarityService similarityService,
                           KafkaProps props
    ) {
        this.consumer = consumer;
        this.producer = producer;
        this.similarityService = similarityService;
        this.props = props;
    }

    @Override
    public void start() {
        running = true;
        consumer.subscribe(List.of(props.getConsumer().getTopic()));
        executor.submit(this::loop);
    }

    private void loop() {
        try {
            while (running) {
                ConsumerRecords<String, SpecificRecordBase> records = consumer.poll(
                        Duration.ofMillis(props.getConsumer().getPollTimeout()));
                for (ConsumerRecord<String, SpecificRecordBase> record : records) {
                    handle(record);
                }
                consumer.commitAsync();
            }
        } catch (WakeupException ignored) {
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            try {
                producer.flush();
                consumer.commitSync();
            } finally {
                consumer.close();
                producer.close();
            }
        }
    }

    private void handle(ConsumerRecord<String, SpecificRecordBase> record) {
        if (record.value() instanceof UserActionAvro action) {
            List<EventSimilarityAvro> list = similarityService.process(action);
            for (EventSimilarityAvro sim : list) {
                producer.send(new ProducerRecord<>(props.getProducer().getTopic(), sim));
            }
        }
    }

    @Override
    public void stop() {
        running = false;
        consumer.wakeup();
        executor.shutdown();
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
