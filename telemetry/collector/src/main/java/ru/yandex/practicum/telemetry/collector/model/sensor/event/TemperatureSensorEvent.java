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
public class TemperatureSensorEvent extends SensorEvent {

    @JsonProperty("temperatureC")
    @NotNull(message = "Temperature in Celsius cannot be null")
    private Integer temperatureC;

    @JsonProperty("temperatureF")
    @NotNull(message = "Temperature in Fahrenheit cannot be null")
    private Integer temperatureF;

    @Override
    public SensorEventType getType() {
        return SensorEventType.TEMPERATURE_SENSOR_EVENT;
    }
}