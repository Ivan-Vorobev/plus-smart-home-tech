package ru.yandex.practicum.telemetry.collector.model.hub.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import ru.yandex.practicum.telemetry.collector.model.enums.HubEventType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DeviceRemovedEvent extends HubEvent {

    @JsonProperty("id")
    @NotBlank(message = "Device ID cannot be blank")
    private String id;

    @Override
    public HubEventType getType() {
        return HubEventType.DEVICE_REMOVED;
    }
}