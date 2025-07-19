package ru.yandex.practicum.telemetry.collector.handler.sensor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.telemetry.collector.handler.SensorEventHandler;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.telemetry.collector.service.KafkaProducerClient;

import java.time.Instant;

import static ru.yandex.practicum.telemetry.collector.configuration.KafkaProducerConfig.TopicType.SENSORS_EVENTS;

@Slf4j
@RequiredArgsConstructor
public abstract class BaseSensorEventHandler<T extends SpecificRecordBase> implements SensorEventHandler {
    // Используем один и тот же продюсер во всех хендлерах
    protected final KafkaProducerClient producer;

    protected abstract T createPayload(SensorEventProto event);

    @Override
    public void handle(SensorEventProto event) {
        // Проверка соответствия типа события ожидаемому типу обработчика
        if (!event.getPayloadCase().equals(getMessageType())) {
            throw new IllegalArgumentException("Unknown sensor event type: " + event.getPayloadCase());
        }

        // Преобразование события в Avro-запись
        T payload = createPayload(event);

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

        // отправка данных в топик Kafka
        producer.send(eventAvro, event.getHubId(), timestamp, SENSORS_EVENTS);
    }
}