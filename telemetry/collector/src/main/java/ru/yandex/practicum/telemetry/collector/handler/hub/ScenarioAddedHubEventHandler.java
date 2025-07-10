package ru.yandex.practicum.telemetry.collector.handler.hub;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.ActionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;
import ru.yandex.practicum.telemetry.collector.model.enums.HubEventType;
import ru.yandex.practicum.telemetry.collector.model.hub.event.HubEvent;
import ru.yandex.practicum.telemetry.collector.model.hub.event.ScenarioAddedEvent;
import ru.yandex.practicum.telemetry.collector.service.KafkaProducerClient;

@Component
public class ScenarioAddedHubEventHandler extends BaseHubEventHandler<ScenarioAddedEventAvro> {
    public ScenarioAddedHubEventHandler(KafkaProducerClient producer) {
        super(producer);
    }

    @Override
    public HubEventType getMessageType() {
        return HubEventType.SCENARIO_ADDED;
    }

    @Override
    protected ScenarioAddedEventAvro createPayload(HubEvent event) {
        ScenarioAddedEvent _event = (ScenarioAddedEvent) event;
        return ScenarioAddedEventAvro.newBuilder()
                .setName(_event.getName())
                .setActions(
                        _event.getActions()
                                .stream()
                                .map(action -> DeviceActionAvro
                                        .newBuilder()
                                        .setSensorId(action.getSensorId())
                                        .setType(ActionTypeAvro.valueOf(action.getType()))
                                        .setValue(action.getValue())
                                        .build())
                                .toList()
                )
                .setConditions(
                        _event.getConditions()
                                .stream()
                                .map(condition -> ScenarioConditionAvro.newBuilder()
                                        .setOperation(ConditionOperationAvro.valueOf(condition.getOperation()))
                                        .setSensorId(condition.getSensorId())
                                        .setType(ConditionTypeAvro.valueOf(condition.getType()))
                                        .setValue(condition.getValue())
                                        .build())
                                .toList()
                )
                .build();
    }
}