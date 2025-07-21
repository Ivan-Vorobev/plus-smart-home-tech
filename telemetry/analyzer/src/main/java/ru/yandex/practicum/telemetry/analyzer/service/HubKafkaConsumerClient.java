package ru.yandex.practicum.telemetry.analyzer.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.telemetry.analyzer.configuration.KafkaConfig;

import java.util.List;
import java.util.Properties;

@Service
public class HubKafkaConsumerClient extends AbstractKafkaConsumerClient<HubEventAvro> {
    public HubKafkaConsumerClient(KafkaConfig kafkaConfig) {
        super(kafkaConfig);
    }

    @Override
    protected Properties getConfig(KafkaConfig kafkaConfig) {
        return kafkaConfig.getHubProperties();
    }

    @Override
    protected List<String> getTopics(KafkaConfig kafkaConfig) {
        return List.of(KafkaConfig.TopicType.HUBS_EVENTS.getTopic());
    }
}
