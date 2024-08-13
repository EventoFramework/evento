package com.evento.application.manager;

import com.evento.application.consumer.ObserverEventConsumer;
import com.evento.application.consumer.SagaEventConsumer;
import com.evento.application.performance.TracingAgent;
import com.evento.application.reference.ObserverReference;
import com.evento.common.utils.Context;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.evento.application.proxy.GatewayTelemetryProxy;
import com.evento.common.messaging.consumer.ConsumerStateStore;
import com.evento.common.modeling.annotations.component.Observer;
import com.evento.common.modeling.messaging.message.application.Message;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * ObserverManager is a class that manages observers for handling events.
 * It extends the ConsumerComponentManager class and overrides its parse method.
 * It also provides a handle method to handle event messages.
 */
public class ObserverManager extends ConsumerComponentManager<ObserverReference> {
    private static final Logger logger = LogManager.getLogger(ObserverManager.class);


    private final ArrayList<ObserverEventConsumer> observerEventConsumers = new ArrayList<>();

    /**
     * Creates a new ObserverManager.
     *
     * @param bundleId              the ID of the bundle
     * @param gatewayTelemetryProxy the function to create a GatewayTelemetryProxy
     * @param tracingAgent          the TracingAgent instance
     * @param isShuttingDown        the function to check if the application is shutting down
     * @param sssFetchSize          the fetch size for SSS requests
     * @param sssFetchDelay         the fetch delay for SSS requests
     */
    public ObserverManager(String bundleId, BiFunction<String, Message<?>, GatewayTelemetryProxy> gatewayTelemetryProxy, TracingAgent tracingAgent, Supplier<Boolean> isShuttingDown, int sssFetchSize, int sssFetchDelay) {
        super(bundleId, gatewayTelemetryProxy, tracingAgent, isShuttingDown, sssFetchSize, sssFetchDelay);
    }

    /**
     * Parses the given Reflections object to find classes annotated with @Observer,
     * creates ObserverReference instances for each class,
     * and registers the ObserverReference in the ObserverManager.
     *
     * @param reflections           the Reflections object to parse
     * @param findInjectableObject  the function to find an injectable object for a given class
     * @throws InvocationTargetException when an error occurs while invoking a method or constructor
     * @throws InstantiationException when a new instance of a class cannot be created
     * @throws IllegalAccessException when access to a class, field, method, or constructor is denied
     */
    @Override
    public void parse(Reflections reflections, Function<Class<?>, Object> findInjectableObject) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        for (Class<?> aClass : reflections.getTypesAnnotatedWith(Observer.class)) {
            var observerReference = new ObserverReference(createComponentInstance(aClass, findInjectableObject));
            getReferences().add(observerReference);
            for (String event : observerReference.getRegisteredEvents()) {
                var hl = getHandlers().getOrDefault(event, new HashMap<>());
                hl.put(aClass.getSimpleName(), observerReference);
                getHandlers().put(event, hl);
                logger.info("Observer event handler for %s found in %s".formatted(event, observerReference.getRef().getClass().getName()));
            }
        }
    }


    /**
     * Starts the event consumers for the observer event listeners.
     *
     * @param consumerStateStore the ConsumerStateStore instance for tracking the state of the consumers
     * @param contexts the component contexts associations
     */
    public void startEventConsumers(ConsumerStateStore consumerStateStore, Map<String, Set<String>> contexts) {
        logger.info("Checking for observer event consumers");
        for (ObserverReference observer : getReferences()) {
            var annotation = observer.getRef().getClass().getAnnotation(Observer.class);
            for (var context : contexts.getOrDefault(observer.getComponentName(), Set.of(Context.ALL))) {
                var observerName = observer.getRef().getClass().getSimpleName();
                var observerVersion = annotation.version();
                logger.info("Starting event consumer for Observer: %s - Version: %d - Context: %s"
                        .formatted(observerName, observerVersion, context));
                var c = new ObserverEventConsumer(
                        getBundleId(),
                        observerName,
                        observerVersion,
                        context,
                        getIsShuttingDown(),
                        consumerStateStore,
                        getHandlers(),
                        getTracingAgent(),
                        getGatewayTelemetryProxy(),
                        getSssFetchSize(),
                        getSssFetchDelay()
                );
                observerEventConsumers.add(c);
                var t = new Thread(c);
                t.setName(observerName + "(v"+observerVersion+") - " + context);
                t.start();
            }

        }

    }

    /**
     * Retrieves the list of ObserverEventConsumer instances associated with the ObserverManager.
     *
     * @return The list of ObserverEventConsumer instances.
     */
    public ArrayList<ObserverEventConsumer> getObserverEventConsumers() {
        return observerEventConsumers;
    }
}
