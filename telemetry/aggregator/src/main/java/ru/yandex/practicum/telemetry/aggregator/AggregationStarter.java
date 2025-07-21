package ru.yandex.practicum.telemetry.aggregator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.telemetry.aggregator.service.AggregatorService;
import ru.yandex.practicum.telemetry.aggregator.service.KafkaConsumerClient;
import ru.yandex.practicum.telemetry.aggregator.service.KafkaProducerClient;

import java.time.Duration;
import java.util.List;

import static ru.yandex.practicum.telemetry.aggregator.configuration.KafkaConfig.TopicType.SENSORS_EVENTS;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {

    private final KafkaProducerClient producerClient;
    private final KafkaConsumerClient consumerClient;
    private final AggregatorService aggregatorService;

    private static final Duration CONSUME_POLL_TIMEOUT = Duration.ofMillis(1000);

    public void start() {
        KafkaConsumer<String, SpecificRecordBase> consumer = consumerClient.getInstance();
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
        try {
            consumer.subscribe(List.of(SENSORS_EVENTS.getTopic()));
            while (true) {
                ConsumerRecords<String, SpecificRecordBase> records = consumer.poll(CONSUME_POLL_TIMEOUT);
                for (ConsumerRecord<String, SpecificRecordBase> record : records) {
                    aggregatorService.aggregationSnapshot(producerClient, record.value());
                }
                consumer.commitAsync();
            }
        } catch (WakeupException ignored) {

        } catch (Exception e) {
            log.error("Error while processing events from sensors", e);
        } finally {
            log.info("Start close Consumer");
            consumerClient.close();
            log.info("Start close Producer");
            producerClient.close();
        }
    }
}
