package ru.yandex.practicum.telemetry.collector.handler;

import ru.yandex.practicum.telemetry.collector.model.enums.HubEventType;
import ru.yandex.practicum.telemetry.collector.model.hub.event.HubEvent;

public interface HubEventHandler {
    HubEventType getMessageType();

    void handle(HubEvent event);
}
