package ru.yandex.practicum.telemetry.aggregator.configuration;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Data
@RequiredArgsConstructor
@Configuration
public class KafkaConfig {
    @Value("${aggregator.kafka.producer.properties}")
    private Properties producerProperties;

    @Value("${aggregator.kafka.consumer.properties}")
    private Properties consumerProperties;

    @Value("${aggregator.kafka.topics}")
    private Map<String, String> topics;

    @PostConstruct
    public void init() {
        TopicType.setTopics(topics);
    }

    public enum TopicType {
        SENSORS_EVENTS("sensors-events"),
        HUBS_EVENTS("hubs-events"),
        SNAPSHOTS_EVENTS("snapshots-events");

        private final String topicKey;

        private static Map<String, String> topics = HashMap.newHashMap(0);

        TopicType(String topicKey) {
            this.topicKey = topicKey;
        }

        public static void setTopics(Map<String, String> topics) {
            TopicType.topics = topics;
        }

        public String getTopic() {
            String topic = topics.get(topicKey);

            if (topic == null) {
                throw new IllegalArgumentException("Incorrect config key value '" + topicKey + "' for kafka topics");
            }

            return topic;
        }
    }
}