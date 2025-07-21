package ru.yandex.practicum.telemetry.aggregator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static ru.yandex.practicum.telemetry.aggregator.configuration.KafkaConfig.TopicType.SNAPSHOTS_EVENTS;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregatorService {
    private final Map<String, SensorsSnapshotAvro> snapshots = new HashMap<>();

    public void aggregationSnapshot(KafkaProducerClient producerClient, SpecificRecordBase sensorEventAvro) {
        SensorEventAvro event = (SensorEventAvro) sensorEventAvro;
        Optional<SensorsSnapshotAvro> updatedSnapshot = updateState(event);
        if (updatedSnapshot.isPresent()) {
            SensorsSnapshotAvro snapshot = updatedSnapshot.get();
            producerClient.send(snapshot, event.getHubId(), snapshot.getTimestamp(), SNAPSHOTS_EVENTS);
        }
    }

    private Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event) {
        SensorsSnapshotAvro snapshot;

        if (!snapshots.containsKey(event.getHubId())) {
            snapshot = new SensorsSnapshotAvro();
            snapshot.setHubId(event.getHubId());
            snapshot.setSensorsState(new HashMap<>());
            snapshots.put(event.getHubId(), snapshot);
        } else {
            snapshot = snapshots.get(event.getHubId());
        }

        SensorStateAvro sensorState = snapshot.getSensorsState().get(event.getId());
        if (!Objects.isNull(sensorState)) {
            if (sensorState.getTimestamp().isAfter(event.getTimestamp())
                    || sensorState.getData().equals(event.getPayload())) {
                return Optional.empty();
            }
        }

        sensorState = SensorStateAvro.newBuilder()
                .setTimestamp(event.getTimestamp())
                .setData(event.getPayload())
                .build();

        snapshot.setTimestamp(event.getTimestamp());
        snapshot.getSensorsState().put(event.getId(), sensorState);
        return Optional.of(snapshot);
    }
}
