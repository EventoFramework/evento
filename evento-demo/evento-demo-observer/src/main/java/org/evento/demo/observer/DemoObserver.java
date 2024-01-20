package org.evento.demo.observer;

import org.evento.common.messaging.gateway.CommandGateway;
import org.evento.common.modeling.annotations.component.Observer;
import org.evento.common.modeling.annotations.handler.EventHandler;
import org.evento.demo.api.event.DemoDeletedEvent;
import org.evento.demo.api.event.DemoUpdatedEvent;
import org.evento.demo.api.utils.Utils;

@Observer(version = 1)
public class DemoObserver {

	@EventHandler
	public void on(DemoUpdatedEvent event, CommandGateway commandGateway) {
		Utils.logMethodFlow(this, "on", event, "OBSERVED");
	}

	@EventHandler
	public void on(DemoDeletedEvent event, CommandGateway commandGateway) {
		Utils.logMethodFlow(this, "on", event, "OBSERVED");
	}
}
