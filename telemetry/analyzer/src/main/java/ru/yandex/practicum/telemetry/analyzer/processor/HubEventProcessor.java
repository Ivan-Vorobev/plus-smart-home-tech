package ru.yandex.practicum.telemetry.analyzer.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.telemetry.analyzer.handler.hub.HubEventHandler;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.telemetry.analyzer.service.HubKafkaConsumerClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class HubEventProcessor implements Runnable {

    private final HubKafkaConsumerClient consumerClient;
    private final Map<String, HubEventHandler> hubEventHandlers;
    private static final Duration CONSUME_POLL_TIMEOUT = Duration.ofMillis(1000);

    public HubEventProcessor(HubKafkaConsumerClient consumerClient, List<HubEventHandler> hubEventHandlers) {
        this.consumerClient = consumerClient;
        this.hubEventHandlers = hubEventHandlers.stream()
                .collect(Collectors.toMap(HubEventHandler::getHubEventType, Function.identity()));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> consumerClient.getInstance().wakeup()));
    }

    @Override
    public void run() {
        try {
            while (true) {
                ConsumerRecords<String, HubEventAvro> records = consumerClient.getInstance().poll(CONSUME_POLL_TIMEOUT);
                if (!records.isEmpty()) {
                    for (ConsumerRecord<String, HubEventAvro> record : records) {
                        HubEventAvro event = record.value();
                        log.info("{}: Payload from kafka: {}", this.getClass().getSimpleName(), event);
                        String eventPayloadName = event.getPayload().getClass().getSimpleName();
                        HubEventHandler eventHandler;

                        if (hubEventHandlers.containsKey(eventPayloadName)) {
                            eventHandler = hubEventHandlers.get(eventPayloadName);
                        } else {
                            log.warn("{}: Unknown type event {}, data {}", this.getClass().getSimpleName(), eventPayloadName, event);
                            continue;
                        }
                        log.info("{}: execute handle {}", this.getClass().getSimpleName(), eventPayloadName);
                        eventHandler.handle(event);

                    }
                    consumerClient.getInstance().commitSync();
                }
            }
        } catch (WakeupException ignored) {

        } catch (Exception e) {
            log.error("{}: Error handle process", this.getClass().getSimpleName(), e);
        } finally {
            consumerClient.getInstance().close();
        }
    }
}
