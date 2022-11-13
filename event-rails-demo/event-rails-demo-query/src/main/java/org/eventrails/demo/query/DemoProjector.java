package org.eventrails.demo.query;

import org.eventrails.demo.api.event.DemoCreatedEvent;
import org.eventrails.demo.api.event.DemoDeletedEvent;
import org.eventrails.demo.api.event.DemoUpdatedEvent;
import org.eventrails.common.modeling.annotations.handler.EventHandler;
import org.eventrails.common.modeling.messaging.message.application.EventMessage;
import org.eventrails.common.modeling.annotations.component.Projector;
import org.eventrails.common.messaging.gateway.QueryGateway;
import org.eventrails.common.modeling.bundle.TransactionalProjector;

@Projector
public class DemoProjector implements TransactionalProjector {

	@EventHandler
	void on(DemoCreatedEvent event, QueryGateway queryGateway, EventMessage eventMessage){
		System.out.println(this.getClass() + " - on(DemoCreatedEvent)");


	}

	@EventHandler
	void on(DemoUpdatedEvent event){
		System.out.println(this.getClass() + " - on(DemoUpdatedEvent)");

	}

	@EventHandler
	void on(DemoDeletedEvent event){
		System.out.println(this.getClass() + " - on(DemoDeletedEvent)");

	}

	@Override
	public void begin() throws Exception {

	}

	@Override
	public void commit() throws Exception {

	}

	@Override
	public void rollback() throws Exception {

	}
}
