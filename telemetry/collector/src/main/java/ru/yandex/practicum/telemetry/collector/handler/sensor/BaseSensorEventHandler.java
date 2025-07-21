package ru.yandex.practicum.telemetry.collector.handler.sensor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import ru.yandex.practicum.telemetry.collector.configuration.KafkaConfig;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.telemetry.collector.handler.SensorEventHandler;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.telemetry.collector.service.KafkaProducerClient;

import java.time.Instant;

@Slf4j
@RequiredArgsConstructor
public abstract class BaseSensorEventHandler<T extends SpecificRecordBase> implements SensorEventHandler {
    // Используем один и тот же продюсер во всех хендлерах
    protected final KafkaProducerClient producer;

    protected abstract T createPayload(SensorEventProto event);

    @Override
    public void handle(SensorEventProto event) {
        log.trace("Get sensor event: " + event);
        // Проверка соответствия типа события ожидаемому типу обработчика
        if (!event.getPayloadCase().equals(getMessageType())) {
            throw new IllegalArgumentException("Unknown sensor event type: " + event.getPayloadCase());
        }

        // Преобразование события в Avro-запись
        T payload = createPayload(event);

        log.trace("Create sensor payload: : " + payload);
        Instant timestamp = Instant.ofEpochSecond(
                event.getTimestamp().getSeconds(),
                event.getTimestamp().getNanos()
        );

        SensorEventAvro eventAvro = SensorEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setId(event.getId())
                .setTimestamp(timestamp)
                .setPayload(payload)
                .build();

        log.trace("Create eventAvro: : " + eventAvro);
        // отправка данных в топик Kafka
        producer.send(eventAvro, event.getHubId(), timestamp, KafkaConfig.TopicType.SENSORS_EVENTS);
    }
}