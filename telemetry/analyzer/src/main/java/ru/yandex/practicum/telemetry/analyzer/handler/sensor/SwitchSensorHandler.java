package ru.yandex.practicum.telemetry.analyzer.handler.sensor;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;
import ru.yandex.practicum.telemetry.analyzer.model.ConditionType;

@Component
public class SwitchSensorHandler implements SensorHandler {
    @Override
    public String getType() {
        return SwitchSensorAvro.class.getSimpleName();
    }

    @Override
    public Integer handle(SensorStateAvro stateAvro, ConditionType type) {
        SwitchSensorAvro sensorAvro = (SwitchSensorAvro) stateAvro.getData();

        return switch (type) {
            case SWITCH -> sensorAvro.getState() ? 1 : 0;
            default -> null;
        };
    }
}
