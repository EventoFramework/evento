package com.evento.common.serialization;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.evento.common.modeling.messaging.payload.Payload;
import com.evento.common.modeling.messaging.query.QueryResponse;
import com.evento.common.modeling.messaging.query.SerializedQueryResponse;
import com.evento.common.modeling.state.AggregateState;
import com.evento.common.modeling.state.SagaState;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The ObjectMapperUtils class provides utility methods for obtaining a shared instance of ObjectMapper for serializing and deserializing objects.
 */
public class ObjectMapperUtils {

	private static ObjectMapper instance;


	/**
	 * Retrieves the shared instance of ObjectMapper used for serializing and deserializing payloads.
	 *
	 * @return The shared instance of ObjectMapper.
	 */
	public synchronized static ObjectMapper getPayloadObjectMapper() {
		if (instance == null)
		{
			PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
					.allowIfSubType(Serializable.class)
					.allowIfSubType("com.evento.")
					.build();

			var om = new ObjectMapper();
			om.registerModule(new JavaTimeModule());
			om.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
			om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			om.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);
			om.setVisibility(om.getSerializationConfig().getDefaultVisibilityChecker()
					.withFieldVisibility(JsonAutoDetect.Visibility.ANY)
					.withGetterVisibility(JsonAutoDetect.Visibility.NONE)
					.withSetterVisibility(JsonAutoDetect.Visibility.NONE)
					.withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
			instance = om;
		}
		return instance;
	}
}
