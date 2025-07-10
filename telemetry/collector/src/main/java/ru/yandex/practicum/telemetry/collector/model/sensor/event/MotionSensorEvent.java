package ru.yandex.practicum.telemetry.collector.model.sensor.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.yandex.practicum.telemetry.collector.model.enums.SensorEventType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class MotionSensorEvent extends SensorEvent {

    @JsonProperty("linkQuality")
    @NotNull(message = "Link quality cannot be null")
    private Integer linkQuality;

    @JsonProperty("motion")
    @NotNull(message = "Motion cannot be null")
    private Boolean motion;

    @JsonProperty("voltage")
    @NotNull(message = "Voltage cannot be null")
    private Integer voltage;

    @Override
    public SensorEventType getType() {
        return SensorEventType.MOTION_SENSOR_EVENT;
    }
}