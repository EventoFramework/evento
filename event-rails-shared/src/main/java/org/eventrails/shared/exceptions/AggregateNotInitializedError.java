package org.eventrails.shared.exceptions;

public class AggregateNotInitializedError extends RuntimeException {


	public AggregateNotInitializedError() {
		super();
	}

	public AggregateNotInitializedError(String message) {
		super(message);
	}

	public static AggregateNotInitializedError build(String aggregateId){
		return new AggregateNotInitializedError("The aggregate %s in not initialized".formatted(aggregateId));
	}
}
