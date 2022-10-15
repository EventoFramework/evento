package org.eventrails.modeling.messaging.message.bus;

import java.io.Serializable;

public class CorrelatedMessage implements Serializable {
		private Serializable body;
		private String correlationId;

		private boolean isResponse;

		public CorrelatedMessage() {
		}

		public CorrelatedMessage(String correlationId, Serializable body, boolean isResponse) {
			this.body = body;
			this.correlationId = correlationId;
			this.isResponse = isResponse;
		}

		public Serializable getBody() {
			return body;
		}

		public void setBody(Serializable body) {
			this.body = body;
		}

		public String getCorrelationId() {
			return correlationId;
		}

		public void setCorrelationId(String correlationId) {
			this.correlationId = correlationId;
		}

		public boolean isResponse() {
			return isResponse;
		}

		public void setResponse(boolean response) {
			isResponse = response;
		}

		public boolean isRequest() {
			return !isResponse;
		}
	}