package org.evento.demo.query;

import org.evento.common.messaging.gateway.QueryGateway;
import org.evento.common.modeling.annotations.component.Projector;
import org.evento.common.modeling.annotations.handler.EventHandler;
import org.evento.common.modeling.messaging.message.application.EventMessage;
import org.evento.demo.api.event.DemoCreatedEvent;
import org.evento.demo.api.event.DemoDeletedEvent;
import org.evento.demo.api.event.DemoUpdatedEvent;
import org.evento.demo.api.utils.Utils;
import org.evento.demo.query.domain.mongo.DemoMongo;
import org.evento.demo.query.domain.mongo.DemoMongoRepository;

import java.time.Instant;

@Projector(version = 2)
public class DemoMongoProjector {

	private final DemoMongoRepository demoMongoRepository;

	public DemoMongoProjector(DemoMongoRepository demoMongoRepository) {
		this.demoMongoRepository = demoMongoRepository;
	}

	@EventHandler
	void on(DemoCreatedEvent event,
			QueryGateway queryGateway,
			EventMessage eventMessage) {
		Utils.logMethodFlow(this, "on", event, "BEGIN");
		var now = Instant.now();
		demoMongoRepository.save(new DemoMongo(event.getDemoId(),
				event.getName(),
				event.getValue(), now, now, null));
		Utils.logMethodFlow(this, "on", event, "END");
	}

	@EventHandler
	void on(DemoUpdatedEvent event) {
		Utils.logMethodFlow(this, "on", event, "BEGIN");
		demoMongoRepository.findById(event.getDemoId()).ifPresent(d -> {
			d.setName(event.getName());
			d.setValue(event.getValue());
			d.setUpdatedAt(Instant.now());
			demoMongoRepository.save(d);
		});
		Utils.logMethodFlow(this, "on", event, "END");
	}

	@EventHandler
	void on(DemoDeletedEvent event) {
		Utils.logMethodFlow(this, "on", event, "BEGIN");
		demoMongoRepository.findById(event.getDemoId()).ifPresent(d -> {
			d.setDeletedAt(Instant.now());
			demoMongoRepository.save(d);
		});
		Utils.logMethodFlow(this, "on", event, "END");

	}
}
