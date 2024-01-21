package org.evento.common.modeling.messaging.message.application;

import org.evento.common.modeling.messaging.payload.Payload;

import java.io.Serializable;
import java.time.Instant;

/**
 * The Message class is an abstract class that represents a message.
 * It contains a serialized payload, timestamp, and metadata.
 * Messages can be subclassed for specific types of messages.
 *
 * @param <T> The type of the payload.
 */
public abstract class Message<T extends Payload> implements Serializable {

	private SerializedPayload<T> serializedPayload;

	private long timestamp;

	private Metadata metadata;

	/**
	 * Constructs a new Message object with the given payload.
	 *
	 * @param payload The payload of the Message.
	 * @param <T>     The type of the payload.
	 */
	public Message(T payload) {
		this.serializedPayload = new SerializedPayload<>(payload);
		this.timestamp = Instant.now().toEpochMilli();
	}

	/**
	 * The Message class represents a message with a serialized payload, timestamp, and metadata.
	 *
	 * @param <T> The type of the payload.
	 */
	public Message() {
	}

	/**
	 * Retrieves the payload of the message.
	 *
	 * @return the payload of the message
	 */
	public T getPayload() {
		return serializedPayload.getObject();
	}

	/**
	 * Sets the payload of the message.
	 *
	 * @param payload The payload to be set.
	 * @param <T>     The type of the payload.
	 */
	public void setPayload(T payload) {
		this.serializedPayload = new SerializedPayload<>(payload);
	}

	/**
	 * Retrieves the serialized payload of the message.
	 *
	 * @return the serialized payload of the message
	 */
	public SerializedPayload<T> getSerializedPayload() {
		return serializedPayload;
	}

	/**
	 * Sets the serialized payload of the Message.
	 *
	 * @param serializedPayload The serialized payload to be set.
	 * @param <T>               The type of the payload.
	 */
	public void setSerializedPayload(SerializedPayload<T> serializedPayload) {
		this.serializedPayload = serializedPayload;
	}

	/**
	 * Retrieves the metadata of the message.
	 *
	 * @return The metadata object associated with the message.
	 */
	public Metadata getMetadata() {
		return metadata;
	}

	/**
	 * Sets the metadata of the message.
	 *
	 * @param metadata The metadata to be set.
	 */
	public void setMetadata(Metadata metadata) {
		this.metadata = metadata;
	}

	/**
	 * Retrieves the type of the message.
	 *
	 * @return the type of the message as a String.
	 */
	public String getType() {
		return serializedPayload.getObjectClass();
	}

	/**
	 * Retrieves the name of the payload.
	 *
	 * @return the name of the payload as a String.
	 */
	public String getPayloadName() {
		var parts = getType().split("\\.");
		return parts[parts.length - 1];
	}

	/**
	 * Retrieves the timestamp of the message.
	 *
	 * @return The timestamp of the message.
	 */
	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * Sets the timestamp of the message.
	 *
	 * @param timestamp The timestamp to be set.
	 */
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
}
