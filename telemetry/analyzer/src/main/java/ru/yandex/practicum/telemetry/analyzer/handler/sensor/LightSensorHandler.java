package ru.yandex.practicum.telemetry.analyzer.handler.sensor;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.telemetry.analyzer.model.ConditionType;

@Component
public class LightSensorHandler implements SensorHandler {
    @Override
    public String getType() {
        return LightSensorAvro.class.getSimpleName();
    }

    @Override
    public Integer handle(SensorStateAvro stateAvro, ConditionType type) {
        LightSensorAvro sensorAvro = (LightSensorAvro) stateAvro.getData();

        return switch (type) {
            case LUMINOSITY -> sensorAvro.getLuminosity();
            default -> null;
        };
    }
}
