package ru.yandex.practicum.telemetry.collector.configuration;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Data
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "collector.kafka.producer")
public class KafkaProducerConfig {
    private Properties properties;

    private Map<String, String> topics;

    public ProducerConfig getProducerConfig() {
        return new ProducerConfig(properties);
    }

    @PostConstruct
    public void init() {
        TopicType.setTopics(topics);
    }

    public enum TopicType {
        SENSORS_EVENTS("sensors-events"),
        HUBS_EVENTS("hubs-events");

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