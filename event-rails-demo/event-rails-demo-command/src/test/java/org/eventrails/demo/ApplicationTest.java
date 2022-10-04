package org.eventrails.demo;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import org.eventrails.application.CommandGatewayImpl;
import org.eventrails.application.EventRailsApplication;
import org.eventrails.demo.api.command.DemoCreateCommand;
import org.eventrails.demo.api.command.DemoDeleteCommand;
import org.eventrails.demo.api.command.DemoUpdateCommand;
import org.eventrails.demo.api.event.DemoCreatedEvent;
import org.eventrails.demo.command.aggregate.DemoAggregateState;
import org.eventrails.modeling.gateway.CommandGateway;
import org.eventrails.modeling.messaging.invocation.AggregateCommandHandlerInvocation;
import org.eventrails.modeling.messaging.message.DomainCommandMessage;
import org.eventrails.modeling.messaging.message.DomainEventMessage;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationTest {

	@Test
	void main() {
		var app = EventRailsApplication.start(Application.class.getPackage().getName(),
				"event-rails-demo-command",
				"event_rails_cluster", 8081,
				new String[0]);
		System.out.println(app);
	}

	@Test
	void cg() {
		var cg = new CommandGatewayImpl("http://localhost:3000");
		DemoCreatedEvent ev = cg.sendAndWait(new DemoCreateCommand("a","b",2));
		System.out.println(ev);
	}

	@Test
	void test() throws JsonProcessingException {

		PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
				.allowIfSubType("org.eventrails")
				.allowIfSubType("java.util.ArrayList")
				.allowIfSubType("java.util.ImmutableCollections")
				.build();

		ObjectMapper mapper = new ObjectMapper();
		mapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);
		mapper.setVisibility(mapper.getSerializationConfig().getDefaultVisibilityChecker()
				.withFieldVisibility(JsonAutoDetect.Visibility.ANY)
				.withGetterVisibility(JsonAutoDetect.Visibility.NONE)
				.withSetterVisibility(JsonAutoDetect.Visibility.NONE)
				.withCreatorVisibility(JsonAutoDetect.Visibility.NONE));

		var demoUpdateCommand = new DemoUpdateCommand("a","a",3);
		var aggregateState = new DemoAggregateState(2);
		var eventStream = List.of(new DomainEventMessage(new DemoCreatedEvent("a","b",1)));
		var a = new AggregateCommandHandlerInvocation(new DomainCommandMessage(demoUpdateCommand), aggregateState, eventStream);
		var json = mapper.writeValueAsString(a);
		System.out.println(json);
		var b = mapper.readValue(json, Object.class);
		System.out.println(b);
	}

	@Test
	public void serverTest(){
		CommandGateway commandGateway = new CommandGatewayImpl("http://localhost:3000");
		var resp = commandGateway.sendAndWait(new DemoCreateCommand("demo_1", "demo1", 0));
		System.out.println(resp);
	}

	@Test
	public void serverTest3(){
		String id = UUID.randomUUID().toString();
		CommandGateway commandGateway = new CommandGatewayImpl("http://localhost:3000");
		var resp = commandGateway.sendAndWait(new DemoCreateCommand(id, id, 0));
		resp = commandGateway.sendAndWait(new DemoUpdateCommand(id, id, 1));
		resp = commandGateway.sendAndWait(new DemoDeleteCommand(id));
		resp = commandGateway.sendAndWait(new DemoDeleteCommand(id));
		System.out.println(resp);
	}

	@Test
	public void serverTest2(){
		CommandGateway commandGateway = new CommandGatewayImpl("http://localhost:3000");
		var resp = commandGateway.sendAndWait(new DemoUpdateCommand("demo_2", "demo2", 0));
		System.out.println(resp);
	}
}