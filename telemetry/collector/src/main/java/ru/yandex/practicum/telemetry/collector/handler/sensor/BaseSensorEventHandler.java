package ru.yandex.practicum.telemetry.collector.handler.sensor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import ru.yandex.practicum.telemetry.collector.handler.SensorEventHandler;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.telemetry.collector.model.sensor.event.SensorEvent;
import ru.yandex.practicum.telemetry.collector.service.KafkaProducerClient;

import static ru.yandex.practicum.telemetry.collector.configuration.KafkaProducerConfig.TopicType.SENSORS_EVENTS;

@Slf4j
@RequiredArgsConstructor
public abstract class BaseSensorEventHandler<T extends SpecificRecordBase> implements SensorEventHandler {
    // Используем один и тот же продюсер во всех хендлерах
    protected final KafkaProducerClient producer;

    protected abstract T createPayload(SensorEvent event);

    @Override
    public void handle(SensorEvent event) {
        // Проверка соответствия типа события ожидаемому типу обработчика
        if (!event.getType().equals(getMessageType())) {
            throw new IllegalArgumentException("Unknown sensor event type: " + event.getType());
        }

        // Преобразование события в Avro-запись
        T payload = createPayload(event);

        SensorEventAvro eventAvro = SensorEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setId(event.getId())
                .setTimestamp(event.getTimestamp())
                .setPayload(payload)
                .build();

        // отправка данных в топик Kafka
        producer.send(eventAvro, event.getHubId(), event.getTimestamp(), SENSORS_EVENTS);
    }
}