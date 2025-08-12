package ru.yandex.practicum.telemetry.analyzer.handler.hub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;
import ru.yandex.practicum.telemetry.analyzer.model.*;
import ru.yandex.practicum.telemetry.analyzer.repository.ScenarioRepository;
import ru.yandex.practicum.telemetry.analyzer.repository.SensorRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScenarioAddedEventHandler implements HubEventHandler {
    private final SensorRepository sensorRepository;
    private final ScenarioRepository scenarioRepository;

    @Override
    public String getHubEventType() {
        return ScenarioAddedEventAvro.class.getSimpleName();
    }

    @Override
    @Transactional
    public void handle(HubEventAvro event) {
        ScenarioAddedEventAvro payload = (ScenarioAddedEventAvro) event.getPayload();
        checkForSensors(event.getHubId(), payload);

        Optional<Scenario> scenarioOpt = scenarioRepository.findByNameAndHubId(payload.getName(), event.getHubId());
        scenarioOpt.ifPresent(
                oldScenario -> scenarioRepository.deleteByHubIdAndName(oldScenario.getHubId(), oldScenario.getName())
        );
        scenarioRepository.flush();


        Scenario scenario = new Scenario();
        scenario.setHubId(event.getHubId());
        scenario.setName(payload.getName());

        Map<String, Condition> conditionMap = payload.getConditions().stream()
                .collect(Collectors.toMap(ScenarioConditionAvro::getSensorId, conditionAvro -> {
                    Condition condition = new Condition();
                    condition.setType(ConditionType.valueOf(conditionAvro.getType().name()));
                    condition.setOperation(ConditionOperation.valueOf(conditionAvro.getOperation().name()));
                    condition.setValue(getValue(conditionAvro.getValue()));
                    return condition;
                }));

        Map<String, Action> actionMap = payload.getActions().stream()
                .collect(Collectors.toMap(DeviceActionAvro::getSensorId, actionAvro -> {
                    Action action = new Action();
                    action.setType(ActionType.valueOf(actionAvro.getType().name()));
                    action.setValue(actionAvro.getValue());
                    return action;
                }));
        scenario.setConditions(conditionMap);
        scenario.setActions(actionMap);

        scenarioRepository.save(scenario);
    }

    private void checkForSensors(String hubId, ScenarioAddedEventAvro payload) {
        List<String> conditionSensorIds = payload.getConditions().stream().map(ScenarioConditionAvro::getSensorId).toList();
        List<String> actionSensorIds = payload.getActions().stream().map(DeviceActionAvro::getSensorId).toList();

        if (!sensorRepository.existsAllByIdInAndHubId(conditionSensorIds, hubId)) {
            throw new IllegalArgumentException("Could not find device in condition list");
        }

        if (!sensorRepository.existsAllByIdInAndHubId(actionSensorIds, hubId)) {
            throw new IllegalArgumentException("Could not find device in action list");
        }
    }

    private Integer getValue(Object value) {
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Boolean) {
            return (Boolean) value ? 1 : 0;
        } else {
            return null;
        }
    }
}
