package org.eventrails.shared;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;

public class ObjectMapperUtils {

	public static ObjectMapper getPayloadObjectMapper(){
		PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
				.allowIfSubType("org.eventrails")
				.allowIfSubType("java.util.ArrayList")
				.allowIfSubType("java.util.ImmutableCollections")
				.build();

		var om = new ObjectMapper();
		om.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);
		om.setVisibility(om.getSerializationConfig().getDefaultVisibilityChecker()
				.withFieldVisibility(JsonAutoDetect.Visibility.ANY)
				.withGetterVisibility(JsonAutoDetect.Visibility.NONE)
				.withSetterVisibility(JsonAutoDetect.Visibility.NONE)
				.withCreatorVisibility(JsonAutoDetect.Visibility.NONE));

		return om;
	}

	public static ObjectMapper getResultObjectMapper(){
		var om = new ObjectMapper();
		om.setVisibility(om.getSerializationConfig().getDefaultVisibilityChecker()
				.withFieldVisibility(JsonAutoDetect.Visibility.ANY)
				.withGetterVisibility(JsonAutoDetect.Visibility.NONE)
				.withSetterVisibility(JsonAutoDetect.Visibility.NONE)
				.withCreatorVisibility(JsonAutoDetect.Visibility.NONE));

		return om;
	}
}
