package ru.yandex.practicum.telemetry.analyzer.handler.hub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.telemetry.analyzer.model.Sensor;
import ru.yandex.practicum.telemetry.analyzer.repository.SensorRepository;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceAddedEventHandler implements HubEventHandler {
    private final SensorRepository sensorRepository;

    @Override
    public String getHubEventType() {
        return DeviceAddedEventAvro.class.getSimpleName();
    }

    @Override
    public void handle(HubEventAvro event) {
        DeviceAddedEventAvro payload = (DeviceAddedEventAvro) event.getPayload();

        Optional<Sensor> oldSensorOpt = sensorRepository.findByIdAndHubId(payload.getId(), event.getHubId());
        if (oldSensorOpt.isEmpty()) {
            log.info("Handle {}, payload: {}", this.getClass().getSimpleName(), payload);
            Sensor sensor = new Sensor();
            sensor.setId(payload.getId());
            sensor.setHubId(event.getHubId());
            sensorRepository.save(sensor);
        } else {
            log.info("Handle {}, device exists: {}", this.getClass().getSimpleName(), oldSensorOpt.get());
        }
    }
}
