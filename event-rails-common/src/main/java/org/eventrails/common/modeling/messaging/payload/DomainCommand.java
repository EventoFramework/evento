package org.eventrails.common.modeling.messaging.payload;

public abstract class DomainCommand extends Command {
	public abstract String getAggregateId();
}
