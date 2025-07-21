package ru.yandex.practicum.telemetry.analyzer.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.telemetry.analyzer.handler.SensorsSnapshotHandler;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.telemetry.analyzer.service.SnapshotKafkaConsumerClient;

import java.time.Duration;

@Slf4j
@Component
public class SnapshotProcessor {
    private final SnapshotKafkaConsumerClient consumerClient;
    private final SensorsSnapshotHandler sensorsSnapshotHandler;

    private static final Duration CONSUME_POLL_TIMEOUT = Duration.ofMillis(1000);

    public SnapshotProcessor(SnapshotKafkaConsumerClient consumerClient, SensorsSnapshotHandler sensorsSnapshotHandler) {
        this.consumerClient = consumerClient;
        this.sensorsSnapshotHandler = sensorsSnapshotHandler;

        Runtime.getRuntime().addShutdownHook(new Thread(() -> consumerClient.getInstance().wakeup()));
    }

    public void start() {
        try {
            while (true) {
                ConsumerRecords<String, SensorsSnapshotAvro> records = consumerClient.getInstance().poll(CONSUME_POLL_TIMEOUT);
                if (!records.isEmpty()) {
                    for (ConsumerRecord<String, SensorsSnapshotAvro> record : records) {

                        log.info("{}: Payload from kafka: {}", this.getClass().getSimpleName(), record);
                        sensorsSnapshotHandler.handle(record.value());

                    }
                    consumerClient.getInstance().commitSync();
                }
            }
        } catch (WakeupException ignored) {

        } catch (Exception e) {
            log.error("{}: Error handle process", this.getClass().getSimpleName(), e);
        } finally {
            consumerClient.close();
        }
    }
}

