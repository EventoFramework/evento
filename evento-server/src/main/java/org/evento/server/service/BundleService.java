package org.evento.server.service;

import org.evento.parser.model.BundleDescription;
import org.evento.parser.model.component.*;
import org.evento.parser.model.handler.*;
import org.evento.parser.model.payload.Command;
import org.evento.parser.model.payload.MultipleResultQueryReturnType;
import org.evento.parser.model.payload.PayloadDescription;
import org.evento.parser.model.payload.Query;
import org.evento.server.domain.model.*;
import org.evento.server.service.deploy.BundleDeployService;
import org.evento.server.domain.model.*;
import org.evento.server.domain.model.Handler;
import org.evento.common.modeling.bundle.types.ComponentType;
import org.evento.common.modeling.bundle.types.HandlerType;
import org.evento.common.modeling.bundle.types.PayloadType;
import org.evento.server.domain.repository.BundleRepository;
import org.evento.server.domain.repository.HandlerRepository;
import org.evento.server.domain.repository.PayloadRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class BundleService {

    private final BundleRepository bundleRepository;

    private final HandlerRepository handlerRepository;
    private final PayloadRepository payloadRepository;

    private final BundleDeployService bundleDeployService;

    public BundleService(BundleRepository bundleRepository, HandlerRepository handlerRepository, PayloadRepository payloadRepository, BundleDeployService bundleDeployService) {
        this.bundleRepository = bundleRepository;
        this.handlerRepository = handlerRepository;
        this.payloadRepository = payloadRepository;
        this.bundleDeployService = bundleDeployService;
    }


    public void register(
            String bundleId,
            BucketType bundleDeploymentBucketType,
            String bundleDeploymentArtifactCoordinates,
            String jarOriginalName,
            BundleDescription bundleDescription) {
        AtomicBoolean isNew = new AtomicBoolean(false);
        var bundle = bundleRepository.findById(bundleId).map(b -> {
            b.setVersion(bundleDescription.getBundleVersion());
            b.setArtifactCoordinates(bundleDeploymentArtifactCoordinates);
            b.setArtifactOriginalName(jarOriginalName);
            b.setContainsHandlers(bundleDescription.getComponents().size() > 0);
            b.setAutorun(bundleDescription.getAutorun());
            b.setMinInstances(bundleDescription.getMinInstances());
            b.setMaxInstances(bundleDescription.getMaxInstances());
            return bundleRepository.save(b);
        }).orElseGet(() -> {
            isNew.set(true);
            return bundleRepository.save(new Bundle(
                    bundleId,
                    bundleDescription.getBundleVersion(),
                    bundleDeploymentBucketType,
                    bundleDeploymentArtifactCoordinates,
                    jarOriginalName,
                    bundleDescription.getComponents().size() > 0,
                    new HashMap<>(),
                    new HashMap<>(),
                    bundleDescription.getAutorun(),
                    bundleDescription.getMinInstances(),
                    bundleDescription.getMaxInstances()));
        });
        if (!isNew.get() && bundle.getVersion() > bundleDescription.getBundleVersion())
            throw new IllegalArgumentException("Bundle " + bundleId + " with version " + bundle.getVersion() + " exists!");

        for (PayloadDescription payloadDescription : bundleDescription.getPayloadDescriptions()) {
            var payload = new Payload();
            payload.setName(payloadDescription.getName());
            payload.setJsonSchema(payloadDescription.getSchema().toString());
            payload.setType(PayloadType.valueOf(payloadDescription.getType()));
            payload.setUpdatedAt(Instant.now());
            payload.setRegisteredIn(bundle.getId());
            payloadRepository.save(payload);
        }

        Bundle finalBundle = bundle;
        for (Component component : bundleDescription.getComponents()) {
            if (component instanceof Aggregate a) {
                for (AggregateCommandHandler aggregateCommandHandler : a.getAggregateCommandHandlers()) {
                    var handler = new org.evento.server.domain.model.Handler();
                    handler.setBundle(bundle);
                    handler.setComponentName(component.getComponentName());
                    handler.setHandlerType(HandlerType.AggregateCommandHandler);
                    handler.setComponentType(ComponentType.Aggregate);
                    handler.setHandledPayload(
                            payloadRepository.findById(aggregateCommandHandler.getPayload().getName())
                                    .map(p -> {
                                        if (p.getType() != PayloadType.DomainCommand) {
                                            p.setType(PayloadType.DomainCommand);
                                            return payloadRepository.save(p);
                                        }
                                        return p;
                                    })
                                    .orElseGet(
                                            () -> {
                                                var payload = new Payload();
                                                payload.setName(aggregateCommandHandler.getPayload().getName());
                                                payload.setJsonSchema("null");
                                                payload.setType(PayloadType.DomainCommand);
                                                payload.setUpdatedAt(Instant.now());
                                                payload.setRegisteredIn(finalBundle.getId());
                                                return payloadRepository.save(payload);
                                            }
                                    ));
                    handler.setReturnIsMultiple(false);
                    handler.setReturnType(payloadRepository.findById(aggregateCommandHandler.getProducedEvent().getName())
                            .map(p -> {
                                if (p.getType() != PayloadType.DomainEvent) {
                                    p.setType(PayloadType.DomainEvent);
                                    return payloadRepository.save(p);
                                }
                                return p;
                            })
                            .orElseGet(
                                    () -> {
                                        var payload = new Payload();
                                        payload.setName(aggregateCommandHandler.getProducedEvent().getName());
                                        payload.setJsonSchema("null");
                                        payload.setType(PayloadType.DomainEvent);
                                        payload.setUpdatedAt(Instant.now());
                                        payload.setRegisteredIn(bundle.getId());
                                        return payloadRepository.save(payload);
                                    }
                            ));
                    handler.setInvocations(new HashMap<>());
                    handler.generateId();
                    handlerRepository.save(handler);
                }
                for (EventSourcingHandler eventSourcingHandler : a.getEventSourcingHandlers()) {
                    var handler = new org.evento.server.domain.model.Handler();
                    handler.setBundle(bundle);
                    handler.setComponentName(component.getComponentName());
                    handler.setHandlerType(HandlerType.EventSourcingHandler);
                    handler.setComponentType(ComponentType.Aggregate);
                    handler.setHandledPayload(payloadRepository.findById(eventSourcingHandler.getPayload().getName())
                            .map(p -> {
                                if (p.getType() != PayloadType.DomainEvent) {
                                    p.setType(PayloadType.DomainEvent);
                                    return payloadRepository.save(p);
                                }
                                return p;
                            })
                            .orElseGet(
                                    () -> {
                                        var payload = new Payload();
                                        payload.setName(eventSourcingHandler.getPayload().getName());
                                        payload.setJsonSchema("null");
                                        payload.setType(PayloadType.DomainEvent);
                                        payload.setUpdatedAt(Instant.now());
                                        payload.setRegisteredIn(bundle.getId());
                                        return payloadRepository.save(payload);
                                    }
                            ));
                    handler.setReturnIsMultiple(false);
                    handler.setReturnType(null);
                    handler.setInvocations(new HashMap<>());
                    handler.generateId();
                    handlerRepository.save(handler);
                }
            } else if (component instanceof Saga s) {
                for (SagaEventHandler sagaEventHandler : s.getSagaEventHandlers()) {
                    var handler = new org.evento.server.domain.model.Handler();
                    handler.setBundle(bundle);
                    handler.setComponentName(component.getComponentName());
                    handler.setHandlerType(HandlerType.SagaEventHandler);
                    handler.setComponentType(ComponentType.Saga);
                    handler.setHandledPayload(payloadRepository.findById(sagaEventHandler.getPayload().getName()).orElseGet(
                            () -> {
                                var payload = new Payload();
                                payload.setName(sagaEventHandler.getPayload().getName());
                                payload.setJsonSchema("null");
                                payload.setType(PayloadType.Event);
                                payload.setUpdatedAt(Instant.now());
                                payload.setRegisteredIn(bundle.getId());
                                return payloadRepository.save(payload);
                            }
                    ));
                    handler.setReturnIsMultiple(false);
                    handler.setReturnType(null);
                    handler.setAssociationProperty(sagaEventHandler.getAssociationProperty());
                    var invocations = new HashMap<Integer, Payload>();
                    for (var command : sagaEventHandler.getCommandInvocations().entrySet()) {
                        invocations.put(
                                command.getKey(),
                                payloadRepository.findById(command.getValue().getName()).orElseGet(
                                () -> {
                                    var payload = new Payload();
                                    payload.setName(command.getValue().getName());
                                    payload.setJsonSchema("null");
                                    payload.setType(PayloadType.Command);
                                    payload.setUpdatedAt(Instant.now());
                                    payload.setRegisteredIn(bundle.getId());
                                    return payloadRepository.save(payload);
                                }
                        ));
                    }
                    for (var query : sagaEventHandler.getQueryInvocations().entrySet()) {
                        invocations.put(query.getKey(), payloadRepository.findById(query.getValue().getName()).orElseGet(
                                () -> {
                                    var payload = new Payload();
                                    payload.setName(query.getValue().getName());
                                    payload.setJsonSchema("null");
                                    payload.setType(PayloadType.Query);
                                    payload.setUpdatedAt(Instant.now());
                                    payload.setRegisteredIn(bundle.getId());
                                    return payloadRepository.save(payload);
                                }
                        ));
                    }
                    handler.setInvocations(invocations);
                    handler.generateId();
                    handlerRepository.save(handler);
                }
            } else if (component instanceof Projection p) {
                for (QueryHandler queryHandler : p.getQueryHandlers()) {
                    var handler = new org.evento.server.domain.model.Handler();
                    handler.setBundle(bundle);
                    handler.setComponentName(component.getComponentName());
                    handler.setHandlerType(HandlerType.QueryHandler);
                    handler.setComponentType(ComponentType.Projection);
                    handler.setHandledPayload(payloadRepository.findById(queryHandler.getPayload().getName()).orElseGet(
                            () -> {
                                var payload = new Payload();
                                payload.setName(queryHandler.getPayload().getName());
                                payload.setJsonSchema("null");
                                payload.setType(PayloadType.Query);
                                payload.setUpdatedAt(Instant.now());
                                payload.setRegisteredIn(bundle.getId());
                                return payloadRepository.save(payload);
                            }
                    ));
                    handler.setReturnIsMultiple(queryHandler.getPayload().getReturnType() instanceof MultipleResultQueryReturnType);
                    handler.setReturnType(payloadRepository.findById(queryHandler.getPayload().getReturnType().getViewName()).orElseGet(
                            () -> {
                                var payload = new Payload();
                                payload.setName(queryHandler.getPayload().getReturnType().getViewName());
                                payload.setJsonSchema("null");
                                payload.setType(PayloadType.View);
                                payload.setUpdatedAt(Instant.now());
                                payload.setRegisteredIn(bundle.getId());
                                return payloadRepository.save(payload);
                            }
                    ));
                    handler.setInvocations(new HashMap<>());
                    handler.generateId();
                    handlerRepository.save(handler);

                }
            } else if (component instanceof Projector p) {
                for (EventHandler eventHandler : p.getEventHandlers()) {
                    var handler = new org.evento.server.domain.model.Handler();
                    handler.setBundle(bundle);
                    handler.setComponentName(component.getComponentName());
                    handler.setHandlerType(HandlerType.EventHandler);
                    handler.setComponentType(ComponentType.Projector);
                    handler.setHandledPayload(payloadRepository.findById(eventHandler.getPayload().getName()).orElseGet(
                            () -> {
                                var payload = new Payload();
                                payload.setName(eventHandler.getPayload().getName());
                                payload.setJsonSchema("null");
                                payload.setType(PayloadType.Event);
                                payload.setUpdatedAt(Instant.now());
                                payload.setRegisteredIn(bundle.getId());
                                return payloadRepository.save(payload);
                            }
                    ));
                    handler.setReturnIsMultiple(false);
                    handler.setReturnType(null);
                    var invocations = new HashMap<Integer, Payload>();
                    for (var query : eventHandler.getQueryInvocations().entrySet()) {
                        invocations.put(query.getKey(),payloadRepository.findById(query.getValue().getName()).orElseGet(
                                () -> {
                                    var payload = new Payload();
                                    payload.setName(query.getValue().getName());
                                    payload.setJsonSchema("null");
                                    payload.setType(PayloadType.Query);
                                    payload.setUpdatedAt(Instant.now());
                                    payload.setRegisteredIn(bundle.getId());
                                    return payloadRepository.save(payload);
                                }
                        ));
                    }
                    handler.setInvocations(invocations);
                    handler.generateId();
                    handlerRepository.save(handler);

                }
            } else if (component instanceof org.evento.parser.model.component.Service s) {
                for (ServiceCommandHandler commandHandler : s.getCommandHandlers()) {
                    var handler = new org.evento.server.domain.model.Handler();
                    handler.setBundle(bundle);
                    handler.setComponentName(component.getComponentName());
                    handler.setHandlerType(HandlerType.CommandHandler);
                    handler.setComponentType(ComponentType.Service);
                    handler.setHandledPayload(payloadRepository.findById(commandHandler.getPayload().getName())
                            .map(p -> {
                                if (p.getType() != PayloadType.ServiceCommand) {
                                    p.setType(PayloadType.ServiceCommand);
                                    return payloadRepository.save(p);
                                }
                                return p;
                            })
                            .orElseGet(
                                    () -> {
                                        var payload = new Payload();
                                        payload.setName(commandHandler.getPayload().getName());
                                        payload.setJsonSchema("null");
                                        payload.setType(PayloadType.ServiceCommand);
                                        payload.setUpdatedAt(Instant.now());
                                        payload.setRegisteredIn(bundle.getId());
                                        return payloadRepository.save(payload);
                                    }
                            ));
                    handler.setReturnIsMultiple(false);
                    handler.setReturnType(commandHandler.getProducedEvent() == null ? null : payloadRepository.findById(commandHandler.getProducedEvent().getName())
                            .map(p -> {
                                if (p.getType() != PayloadType.ServiceEvent) {
                                    p.setType(PayloadType.ServiceEvent);
                                    return payloadRepository.save(p);
                                }
                                return p;
                            })
                            .orElseGet(
                                    () -> {
                                        var payload = new Payload();
                                        payload.setName(commandHandler.getProducedEvent().getName());
                                        payload.setJsonSchema("null");
                                        payload.setType(PayloadType.ServiceEvent);
                                        payload.setUpdatedAt(Instant.now());
                                        payload.setRegisteredIn(bundle.getId());
                                        return payloadRepository.save(payload);
                                    }
                            ));
                    var invocations = new HashMap<Integer, Payload>();
                    for (var query : commandHandler.getQueryInvocations().entrySet()) {
                        invocations.put(query.getKey(), payloadRepository.findById(query.getValue().getName()).orElseGet(
                                () -> {
                                    var payload = new Payload();
                                    payload.setName(query.getValue().getName());
                                    payload.setJsonSchema("null");
                                    payload.setType(PayloadType.Query);
                                    payload.setUpdatedAt(Instant.now());
                                    payload.setRegisteredIn(bundle.getId());
                                    return payloadRepository.save(payload);
                                }
                        ));
                    }
                    for (var command : commandHandler.getCommandInvocations().entrySet()) {
                        invocations.put(command.getKey(),payloadRepository.findById(command.getValue().getName()).orElseGet(
                                () -> {
                                    var payload = new Payload();
                                    payload.setName(command.getValue().getName());
                                    payload.setJsonSchema("null");
                                    payload.setType(PayloadType.Command);
                                    payload.setUpdatedAt(Instant.now());
                                    payload.setRegisteredIn(bundle.getId());
                                    return payloadRepository.save(payload);
                                }
                        ));
                    }
                    handler.setInvocations(invocations);
                    handler.generateId();
                    handlerRepository.save(handler);
                }
            } else if (component instanceof Invoker i) {
                for (InvocationHandler invocationHandler : i.getInvocationHandlers()) {
                    var handler = new org.evento.server.domain.model.Handler();
                    handler.setBundle(bundle);
                    handler.setComponentName(component.getComponentName());
                    handler.setHandlerType(HandlerType.InvocationHandler);
                    handler.setComponentType(ComponentType.Invoker);
                    handler.setHandledPayload(payloadRepository.getById(invocationHandler.getPayload().getName()));
                    handler.setReturnIsMultiple(false);
                    handler.setReturnType(null);
                    var invocations = new HashMap<Integer, Payload>();
                    for (var query : invocationHandler.getQueryInvocations().entrySet()) {
                        invocations.put(query.getKey(), payloadRepository.findById(query.getValue().getName()).orElseGet(
                                () -> {
                                    var payload = new Payload();
                                    payload.setName(query.getValue().getName());
                                    payload.setJsonSchema("null");
                                    payload.setType(PayloadType.Query);
                                    payload.setUpdatedAt(Instant.now());
                                    payload.setRegisteredIn(bundle.getId());
                                    return payloadRepository.save(payload);
                                }
                        ));
                    }
                    for (var command : invocationHandler.getCommandInvocations().entrySet()) {
                        invocations.put(
                                command.getKey(),
                                payloadRepository.findById(command.getValue().getName()).orElseGet(
                                () -> {
                                    var payload = new Payload();
                                    payload.setName(command.getValue().getName());
                                    payload.setJsonSchema("null");
                                    payload.setType(PayloadType.Command);
                                    payload.setUpdatedAt(Instant.now());
                                    payload.setRegisteredIn(bundle.getId());
                                    return payloadRepository.save(payload);
                                }
                        ));
                    }
                    handler.setInvocations(invocations);
                    handler.generateId();
                    handlerRepository.save(handler);
                }
            }
        }

        if (bundle.isAutorun() && bundle.getBucketType() != BucketType.Ephemeral) {
            try {
                if (isNew.get())
                    new Thread(() -> bundleDeployService.waitUntilAvailable(bundleId)).start();
                else
                    bundleDeployService.spawn(bundleId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }

    public void unregister(
            String bundleDeploymentName) {
        for (Handler handler : handlerRepository.findAll()) {
            if (!handler.getBundle().getId().equals(bundleDeploymentName)) continue;
            handlerRepository.delete(handler);
            handler.getHandledPayload().getHandlers().remove(handler);
        }

        bundleRepository.findById(bundleDeploymentName).ifPresent(bundleRepository::delete);
        for (Payload payload : payloadRepository.findAll()) {
            try {
                if (!bundleRepository.existsById(payload.getRegisteredIn()))
                    payloadRepository.delete(payload);
            } catch (Exception ignored) {
            }
        }
    }

    public List<Bundle> findAllBundles() {
        return bundleRepository.findAll();
    }

    public Bundle findByName(String bundleId) {
        return bundleRepository.findById(bundleId).orElseThrow();
    }

    public void putEnv(String bundleId, String key, String value) {
        var bundle = bundleRepository.findById(bundleId).orElseThrow();
        bundle.getEnvironment().put(key, value);
        bundleRepository.save(bundle);
    }

    public void removeEnv(String bundleId, String key) {
        var bundle = bundleRepository.findById(bundleId).orElseThrow();
        bundle.getEnvironment().remove(key);
        bundleRepository.save(bundle);
    }

    public void putVmOption(String bundleId, String key, String value) {
        var bundle = bundleRepository.findById(bundleId).orElseThrow();
        bundle.getVmOptions().put(key, value);
        bundleRepository.save(bundle);
    }

    public void removeVmOption(String bundleId, String key) {
        var bundle = bundleRepository.findById(bundleId).orElseThrow();
        bundle.getVmOptions().remove(key);
        bundleRepository.save(bundle);
    }
}
