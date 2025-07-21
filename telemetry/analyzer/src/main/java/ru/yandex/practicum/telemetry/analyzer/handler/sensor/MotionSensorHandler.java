package ru.yandex.practicum.telemetry.analyzer.handler.sensor;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.telemetry.analyzer.model.ConditionType;

@Component
public class MotionSensorHandler implements SensorHandler {
    @Override
    public String getType() {
        return MotionSensorAvro.class.getSimpleName();
    }

    @Override
    public Integer handle(SensorStateAvro stateAvro, ConditionType type) {
        MotionSensorAvro sensorAvro = (MotionSensorAvro) stateAvro.getData();

        return switch (type) {
            case MOTION -> sensorAvro.getMotion() ? 1 : 0;
            default -> null;
        };
    }
}
