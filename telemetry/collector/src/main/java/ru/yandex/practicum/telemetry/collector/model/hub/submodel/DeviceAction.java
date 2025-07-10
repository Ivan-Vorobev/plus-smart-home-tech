package ru.yandex.practicum.telemetry.collector.model.hub.submodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceAction {

    @JsonProperty("sensorId")
    @NotBlank(message = "Sensor ID cannot be blank")
    private String sensorId;

    @JsonProperty("type")
    @NotBlank(message = "Action type cannot be blank")
    private String type;

    @JsonProperty("value")
    private Integer value;
}