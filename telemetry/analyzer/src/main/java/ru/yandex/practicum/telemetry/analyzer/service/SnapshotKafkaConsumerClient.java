package ru.yandex.practicum.telemetry.analyzer.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.telemetry.analyzer.configuration.KafkaConfig;

import java.util.List;
import java.util.Properties;

@Service
public class SnapshotKafkaConsumerClient extends AbstractKafkaConsumerClient<SensorsSnapshotAvro> {
    public SnapshotKafkaConsumerClient(KafkaConfig kafkaConfig) {
        super(kafkaConfig);
    }

    @Override
    protected Properties getConfig(KafkaConfig kafkaConfig) {
        return kafkaConfig.getSnapshotProperties();
    }

    @Override
    protected List<String> getTopics(KafkaConfig kafkaConfig) {
        return List.of(KafkaConfig.TopicType.SNAPSHOTS_EVENTS.getTopic());
    }
}
