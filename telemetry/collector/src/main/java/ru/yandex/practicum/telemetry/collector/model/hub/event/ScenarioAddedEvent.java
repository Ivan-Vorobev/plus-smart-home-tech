package ru.yandex.practicum.telemetry.collector.model.hub.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import ru.yandex.practicum.telemetry.collector.model.enums.HubEventType;
import ru.yandex.practicum.telemetry.collector.model.hub.submodel.DeviceAction;
import ru.yandex.practicum.telemetry.collector.model.hub.submodel.ScenarioCondition;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ScenarioAddedEvent extends HubEvent {

    @JsonProperty("name")
    @NotBlank(message = "Scenario name cannot be blank")
    @Size(min = 3, message = "Scenario name must be at least 3 characters long")
    private String name;

    @JsonProperty("conditions")
    @NotNull(message = "Conditions cannot be null")
    @Size(min = 1, message = "Conditions list cannot be empty")
    private List<ScenarioCondition> conditions;

    @JsonProperty("actions")
    @NotNull(message = "Actions cannot be null")
    @Size(min = 1, message = "Actions list cannot be empty")
    private List<DeviceAction> actions;

    @Override
    public HubEventType getType() {
        return HubEventType.SCENARIO_ADDED;
    }
}