package com.evento.application.reference;


import com.evento.application.utils.ReflectionUtils;
import com.evento.common.messaging.gateway.CommandGateway;
import com.evento.common.messaging.gateway.QueryGateway;
import com.evento.common.modeling.annotations.handler.CommandHandler;
import com.evento.common.modeling.messaging.message.application.CommandMessage;
import com.evento.common.modeling.messaging.payload.ServiceCommand;
import com.evento.common.modeling.messaging.payload.ServiceEvent;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

/**
 * The ServiceReference class represents a reference to a service object. It extends the Reference class.
 */
public class ServiceReference extends Reference {


    private final HashMap<String, Method> serviceCommandHandlerReferences = new HashMap<>();

    /**
     * The ServiceReference class represents a reference to a service object. It extends the Reference class.
     * @param ref The reference to the service object
     */
    public ServiceReference(Object ref) {
        super(ref);
        for (Method declaredMethod : ref.getClass().getDeclaredMethods()) {

            var ach = declaredMethod.getAnnotation(CommandHandler.class);
            if (ach != null) {
                serviceCommandHandlerReferences.put(Arrays.stream(declaredMethod.getParameterTypes())
                        .filter(ServiceCommand.class::isAssignableFrom)
                        .findFirst()
                        .map(Class::getSimpleName)
                        .orElseThrow(() -> new IllegalArgumentException("ServiceCommand parameter not fount in  " + declaredMethod)), declaredMethod);
            }
        }
    }


    /**
     * Retrieves the aggregate command handler method associated with the specified event name.
     *
     * @param eventName The name of the event.
     * @return The aggregate command handler method associated with the event name, or null if not found.
     */
    public Method getAggregateCommandHandler(String eventName) {
        return serviceCommandHandlerReferences.get(eventName);
    }

    /**
     * Retrieves the set of registered command names.
     *
     * @return A set containing the names of the registered commands.
     */
    public Set<String> getRegisteredCommands() {
        return serviceCommandHandlerReferences.keySet();
    }

    /**
     * Invokes the command handler method associated with the specified command message, using the given command gateway
     * and query gateway.
     *
     * @param cm             The CommandMessage containing the command name and payload.
     * @param commandGateway The CommandGateway used for sending commands.
     * @param queryGateway   The QueryGateway used for sending queries.
     * @return The ServiceEvent generated by the command handler method.
     * @throws Exception If an error occurs during the invocation.
     */
    public ServiceEvent invoke(
            CommandMessage<? extends ServiceCommand> cm,
            CommandGateway commandGateway,
            QueryGateway queryGateway)
            throws Exception {

        var commandHandler = serviceCommandHandlerReferences.get(cm.getCommandName());

        var resp =  (ServiceEvent) ReflectionUtils.invoke(getRef(), commandHandler,
                cm.getPayload(),
                commandGateway,
                queryGateway,
                cm,
                cm.getMetadata()
        );
        if(resp != null){
            if(resp.getAggregateId() == null){
                return resp.setAggregateId(cm.getPayload());
            }
        }
        return resp;
    }
}
