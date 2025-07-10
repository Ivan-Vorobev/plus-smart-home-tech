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
public class ClimateSensorEvent extends SensorEvent {

    @JsonProperty("temperatureC")
    @NotNull(message = "Temperature in Celsius cannot be null")
    private Integer temperatureC;

    @JsonProperty("humidity")
    @NotNull(message = "Humidity cannot be null")
    private Integer humidity;

    @JsonProperty("co2Level")
    @NotNull(message = "CO2 level cannot be null")
    private Integer co2Level;

    @Override
    public SensorEventType getType() {
        return SensorEventType.CLIMATE_SENSOR_EVENT;
    }
}