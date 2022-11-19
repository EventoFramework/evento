package org.eventrails.bus.rabbitmq;

import org.eventrails.common.modeling.messaging.message.bus.NodeAddress;

public class RabbitMqNodeAddress extends NodeAddress {
	public RabbitMqNodeAddress(String bundleId, long bundleVersion, Object address, String nodeId) {
		super(bundleId, bundleVersion, address, nodeId);
	}

	@Override
	public int compareTo(NodeAddress o) {
		return getNodeId().compareTo(o.getNodeId());
	}
}
