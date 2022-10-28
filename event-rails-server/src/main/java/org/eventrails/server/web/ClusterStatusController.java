package org.eventrails.server.web;

import org.eventrails.modeling.messaging.message.bus.MessageBus;
import org.eventrails.modeling.messaging.message.bus.NodeAddress;
import org.eventrails.server.domain.model.Ranch;
import org.eventrails.server.service.RanchApplicationService;
import org.eventrails.server.service.RanchDeployService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/cluster-status")
public class ClusterStatusController {

	private final Logger logger = LoggerFactory.getLogger(ClusterStatusController.class);

	@Value("${eventrails.cluster.node.server.name}")
	private String serverNodeName;
	private final MessageBus messageBus;

	private final RanchApplicationService ranchApplicationService;
	private final RanchDeployService ranchDeployService;

	public ClusterStatusController(MessageBus messageBus,
								   RanchApplicationService ranchApplicationService, RanchDeployService ranchDeployService) {
		this.messageBus = messageBus;
		this.ranchApplicationService = ranchApplicationService;
		this.ranchDeployService = ranchDeployService;
	}

	@GetMapping(value = "/attended-view")
	public ResponseEntity<List<String>> findAllNodes() {
		var nodes = ranchApplicationService.findAllRanches().stream().filter(Ranch::isContainsHandlers).map(Ranch::getName).collect(Collectors.toList());
		nodes.add(serverNodeName);
		return ResponseEntity.ok(nodes);
	}

	@GetMapping(value = "/view")
	public SseEmitter handle() throws IOException {
		SseEmitter emitter = new SseEmitter(15 * 60 * 1000L);
		emitter.send(messageBus.getCurrentView());
		var listener = new Consumer<List<NodeAddress>>() {
			@Override
			public void accept(List<NodeAddress> o) {
				try
				{
					emitter.send(o);
				} catch (Exception e)
				{
					logger.warn("Listener Error", e);
					messageBus.removeViewListener(this);
				}
			}
		};
		messageBus.addViewListener(listener);
		return emitter;
	}

	@GetMapping(value = "/available-view")
	public SseEmitter handleViewEnabled() throws IOException {
		SseEmitter emitter = new SseEmitter(15 * 60 * 1000L);
		emitter.send(messageBus.getCurrentAvailableView());
		var listener = new Consumer<List<NodeAddress>>() {
			@Override
			public void accept(List<NodeAddress> o) {
				try
				{
					emitter.send(o);
				} catch (Exception e)
				{
					logger.warn("Listener Error", e);
					messageBus.removeAvailableViewListener(this);
				}
			}
		};
		messageBus.addAvailableViewListener(listener);
		return emitter;
	}

	@PostMapping(value = "/spawn/{ranchName}")
	public ResponseEntity<?> spawnRanch(@PathVariable String ranchName) throws Exception {
		ranchDeployService.spawn(ranchName);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping(value = "/kill/{nodeId}")
	public ResponseEntity<?> killNode(@PathVariable String nodeId) throws Exception {
		ranchDeployService.kill(nodeId);
		return ResponseEntity.ok().build();
	}
}
