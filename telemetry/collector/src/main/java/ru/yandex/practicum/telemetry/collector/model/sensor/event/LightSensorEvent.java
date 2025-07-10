package ru.yandex.practicum.telemetry.collector.model.sensor.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import ru.yandex.practicum.telemetry.collector.model.enums.SensorEventType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class LightSensorEvent extends SensorEvent {

    @JsonProperty("linkQuality")
    private Integer linkQuality;

    @JsonProperty("luminosity")
    private Integer luminosity;

    @Override
    public SensorEventType getType() {
        return SensorEventType.LIGHT_SENSOR_EVENT;
    }
}