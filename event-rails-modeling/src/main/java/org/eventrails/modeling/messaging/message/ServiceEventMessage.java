package org.eventrails.modeling.messaging.message;

import org.eventrails.modeling.messaging.payload.ServiceEvent;

public class ServiceEventMessage extends EventMessage<ServiceEvent> {
	public ServiceEventMessage(ServiceEvent payload) {
		super(payload);
	}
}
