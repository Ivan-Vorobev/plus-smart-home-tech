package ru.yandex.practicum.telemetry.collector.controller;


import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.telemetry.collector.handler.HubEventHandler;
import ru.yandex.practicum.telemetry.collector.handler.SensorEventHandler;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@GrpcService
public class EventController extends CollectorControllerGrpc.CollectorControllerImplBase {
    private final Map<SensorEventProto.PayloadCase, SensorEventHandler> sensorEventHandlers;
    private final Map<HubEventProto.PayloadCase, HubEventHandler> hubEventHandlers;

    public EventController(
            List<SensorEventHandler> sensorEventHandlers,
            List<HubEventHandler> hubEventHandlers
    ) {
        this.sensorEventHandlers = sensorEventHandlers.stream()
                .collect(Collectors.toMap(SensorEventHandler::getMessageType, Function.identity()));
        this.hubEventHandlers = hubEventHandlers.stream()
                .collect(Collectors.toMap(HubEventHandler::getMessageType, Function.identity()));
    }

    @Override
    public void collectSensorEvent(SensorEventProto request, StreamObserver<Empty> responseObserver) {
        try {
            if (sensorEventHandlers.containsKey(request.getPayloadCase())) {
                sensorEventHandlers.get(request.getPayloadCase()).handle(request);
            } else {
                throw new IllegalArgumentException("Could not find sensor event handler:  " + request.getPayloadCase());
            }
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(new StatusRuntimeException(Status.fromThrowable(e)));
        }

    }

    @Override
    public void collectHubEvent(HubEventProto request, StreamObserver<Empty> responseObserver) {
        try {
            if (hubEventHandlers.containsKey(request.getPayloadCase())) {
                hubEventHandlers.get(request.getPayloadCase()).handle(request);
            } else {
                throw new IllegalArgumentException("Could not find hub event handler: " + request.getPayloadCase());
            }
        } catch (Exception e) {
            responseObserver.onError(new StatusRuntimeException(Status.fromThrowable(e)));
        }
    }
}
