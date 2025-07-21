package ru.yandex.practicum.telemetry.collector.handler.hub;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioAddedEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioConditionProto;
import ru.yandex.practicum.kafka.telemetry.event.ActionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;
import ru.yandex.practicum.telemetry.collector.service.KafkaProducerClient;

@Component
public class ScenarioAddedHubEventHandler extends BaseHubEventHandler<ScenarioAddedEventAvro> {
    public ScenarioAddedHubEventHandler(KafkaProducerClient producer) {
        super(producer);
    }

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.SCENARIO_ADDED;
    }

    @Override
    protected ScenarioAddedEventAvro createPayload(HubEventProto event) {
        ScenarioAddedEventProto _event = event.getScenarioAdded();
        return ScenarioAddedEventAvro.newBuilder()
                .setName(_event.getName())
                .setActions(
                        _event.getActionList()
                                .stream()
                                .map(action -> DeviceActionAvro
                                        .newBuilder()
                                        .setSensorId(action.getSensorId())
                                        .setType(ActionTypeAvro.valueOf(action.getType().name()))
                                        .setValue(action.getValue())
                                        .build())
                                .toList()
                )
                .setConditions(
                        _event.getConditionList()
                                .stream()
                                .map(condition -> ScenarioConditionAvro.newBuilder()
                                        .setOperation(ConditionOperationAvro.valueOf(condition.getOperation().name()))
                                        .setSensorId(condition.getSensorId())
                                        .setType(ConditionTypeAvro.valueOf(condition.getType().name()))
                                        .setValue(
                                                condition.getValueCase().equals(ScenarioConditionProto.ValueCase.BOOL_VALUE)
                                                        ? condition.getBoolValue()
                                                        : condition.getIntValue()
                                        )
                                        .build())
                                .toList()
                )
                .build();
    }
}