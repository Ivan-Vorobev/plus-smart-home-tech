package ru.yandex.practicum.telemetry.collector.handler.sensor;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.telemetry.collector.model.enums.SensorEventType;
import ru.yandex.practicum.telemetry.collector.model.sensor.event.MotionSensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.event.SensorEvent;
import ru.yandex.practicum.telemetry.collector.service.KafkaProducerClient;

@Component
public class MotionSensorEventHandler extends BaseSensorEventHandler<MotionSensorAvro> {

    public MotionSensorEventHandler(KafkaProducerClient producer) {
        super(producer);
    }

    @Override
    public SensorEventType getMessageType() {
        return SensorEventType.MOTION_SENSOR_EVENT;
    }

    @Override
    protected MotionSensorAvro createPayload(SensorEvent event) {
        MotionSensorEvent payload = (MotionSensorEvent) event;
        return MotionSensorAvro.newBuilder()
                .setLinkQuality(payload.getLinkQuality())
                .setVoltage(payload.getVoltage())
                .setMotion(payload.getMotion())
                .build();
    }
}