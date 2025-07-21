package ru.yandex.practicum.telemetry.analyzer.handler.sensor;

import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.telemetry.analyzer.model.ConditionType;

public interface SensorHandler {
    String getType();

    Integer handle(SensorStateAvro stateAvro, ConditionType type);
}
