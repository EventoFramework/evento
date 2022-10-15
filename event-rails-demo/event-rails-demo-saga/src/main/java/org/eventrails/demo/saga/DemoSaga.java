package org.eventrails.demo.saga;

import org.eventrails.demo.api.command.NotificationSendCommand;
import org.eventrails.demo.api.command.NotificationSendSilentCommand;
import org.eventrails.demo.api.event.DemoCreatedEvent;
import org.eventrails.demo.api.event.DemoDeletedEvent;
import org.eventrails.demo.api.event.DemoUpdatedEvent;
import org.eventrails.demo.api.event.NotificationSentEvent;
import org.eventrails.demo.api.query.DemoViewFindByIdQuery;
import org.eventrails.demo.api.view.DemoRichView;
import org.eventrails.demo.api.view.DemoView;
import org.eventrails.modeling.annotations.component.Saga;
import org.eventrails.modeling.annotations.handler.SagaEventHandler;
import org.eventrails.modeling.gateway.CommandGateway;
import org.eventrails.modeling.gateway.QueryGateway;
import org.eventrails.modeling.messaging.message.EventMessage;

import java.util.concurrent.ExecutionException;

@Saga
public class DemoSaga {

	@SagaEventHandler(init = true, associationProperty = "demoId")
	public DemoSagaState on(DemoCreatedEvent event,
							CommandGateway commandGateway,
							QueryGateway queryGateway,
							EventMessage<?> message) {
		System.out.println(this.getClass() + " - on(DemoCreatedEvent)");
		DemoSagaState demoSagaState = new DemoSagaState();
		demoSagaState.setAssociation("demoId", event.getDemoId());
		demoSagaState.setLastValue(event.getValue());
		return demoSagaState;
	}

	@SagaEventHandler(associationProperty = "demoId")
	public DemoSagaState on(DemoUpdatedEvent event,
							DemoSagaState demoSagaState,
							CommandGateway commandGateway,
							QueryGateway queryGateway,
							EventMessage<?> message) throws ExecutionException, InterruptedException {
		System.out.println(this.getClass() + " - on(DemoUpdatedEvent)");

		if (event.getValue() - demoSagaState.getLastValue() > 10)
		{
			var demo = queryGateway.query(new DemoViewFindByIdQuery(event.getDemoId())).get();

			System.out.println(jump(commandGateway, demo.toString()));
		}

		demoSagaState.setLastValue(event.getValue());
		return demoSagaState;
	}

	@SagaEventHandler(associationProperty = "demoId")
	public DemoSagaState on(DemoDeletedEvent event,
							DemoSagaState demoSagaState,
							CommandGateway commandGateway,
							QueryGateway queryGateway,
							EventMessage<?> message) throws ExecutionException, InterruptedException {
		System.out.println(this.getClass() + " - on(DemoDeletedEvent)");


		var demo = queryGateway.query(new DemoViewFindByIdQuery(event.getDemoId())).get();
		var resp = commandGateway.send(new NotificationSendSilentCommand("lol" + demo.getData().getDemoId())).get();
		System.out.println(resp);
		 

		demoSagaState.setEnded(true);
		return demoSagaState;
	}
	public NotificationSentEvent jump(CommandGateway commandGateway, String msg) {
		return sendNotification(commandGateway, msg);
	}

	public NotificationSentEvent sendNotification(CommandGateway commandGateway, String msg){
		return commandGateway.sendAndWait(new NotificationSendCommand(msg));
	}


}
