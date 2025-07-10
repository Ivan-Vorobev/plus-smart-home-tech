package ru.yandex.practicum.telemetry.collector.model.hub.submodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScenarioCondition {

    @JsonProperty("sensorId")
    @NotBlank(message = "Sensor ID cannot be blank")
    private String sensorId;

    @JsonProperty("type")
    @NotBlank(message = "Condition type cannot be blank")
    private String type;

    @JsonProperty("operation")
    @NotBlank(message = "Operation cannot be blank")
    private String operation;

    @JsonProperty("value")
    @NotNull(message = "Value cannot be null")
    private Integer value;
}