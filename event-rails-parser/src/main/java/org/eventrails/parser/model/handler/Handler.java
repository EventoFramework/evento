package org.eventrails.parser.model.handler;

import org.eventrails.parser.model.payload.Payload;

import java.io.Serializable;

public class Handler<T extends Payload> implements Serializable {
	private T payload;

	public Handler(T payload) {
		this.payload = payload;
	}

	public Handler() {
	}

	public T getPayload() {
		return payload;
	}

	public void setPayload(T payload) {
		this.payload = payload;
	}
}
