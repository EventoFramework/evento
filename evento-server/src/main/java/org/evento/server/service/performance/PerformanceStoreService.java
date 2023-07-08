package org.evento.server.service.performance;

import org.evento.common.performance.PerformanceInvocationsMessage;
import org.evento.common.performance.PerformanceService;
import org.evento.common.performance.PerformanceServiceTimeMessage;
import org.evento.server.domain.model.Handler;
import org.evento.server.domain.model.Payload;
import org.evento.server.performance.HandlerInvocationCountPerformance;
import org.evento.server.performance.HandlerServiceTimePerformance;
import org.evento.server.domain.repository.ComponentRepository;
import org.evento.server.domain.repository.HandlerInvocationCountPerformanceRepository;
import org.evento.server.domain.repository.HandlerRepository;
import org.evento.server.domain.repository.HandlerServiceTimePerformanceRepository;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
public class PerformanceStoreService extends PerformanceService {

	public static final double ALPHA = 0.33;
	private final HandlerServiceTimePerformanceRepository handlerServiceTimePerformanceRepository;
	private final HandlerInvocationCountPerformanceRepository handlerInvocationCountPerformanceRepository;

	private final HandlerRepository handlerRepository;

	private final LockRegistry lockRegistry;

	public PerformanceStoreService(HandlerServiceTimePerformanceRepository handlerServiceTimePerformanceRepository, HandlerInvocationCountPerformanceRepository handlerInvocationCountPerformanceRepository, HandlerRepository handlerRepository, ComponentRepository componentRepository, LockRegistry lockRegistry) {
		this.handlerServiceTimePerformanceRepository = handlerServiceTimePerformanceRepository;
		this.handlerInvocationCountPerformanceRepository = handlerInvocationCountPerformanceRepository;
		this.handlerRepository = handlerRepository;

		this.lockRegistry = lockRegistry;
	}

	public static Instant now() {
		return Instant.now();
	}

	public Double getMeanServiceTime(String bundle, String component, String action) {
		return handlerServiceTimePerformanceRepository.findById(bundle + "_" + component + "_" + action).map(
				HandlerServiceTimePerformance::getAgedMeanServiceTime
		).orElse(null);
	}

	public void saveServiceTimePerformance(String bundle, String component, String action, long start, long end) {
		var pId = bundle + "_" + component + "_" + action;
		var lock = lockRegistry.obtain(pId);
		var duration = end - start;
		if (lock.tryLock())
		{
			try
			{
				var hp = handlerServiceTimePerformanceRepository.findById(pId);
				HandlerServiceTimePerformance handlerServiceTimePerformance;
				if (hp.isPresent())
				{
					handlerServiceTimePerformance = hp.get();
					handlerServiceTimePerformance.setAgedMeanServiceTime((((duration) * (1 - ALPHA)) + handlerServiceTimePerformance.getAgedMeanServiceTime() * ALPHA));
					handlerServiceTimePerformance.setLastServiceTime(duration);
					handlerServiceTimePerformance.setMaxServiceTime(Math.max(handlerServiceTimePerformance.getMaxServiceTime(), duration));
					handlerServiceTimePerformance.setMinServiceTime(Math.min(handlerServiceTimePerformance.getMinServiceTime(), duration));

					if (handlerServiceTimePerformance.getLastArrival() < start)
					{
						var interval = start - handlerServiceTimePerformance.getLastArrival();
						handlerServiceTimePerformance.setAgedMeanArrivalInterval((((duration) * (1 - ALPHA)) + handlerServiceTimePerformance.getAgedMeanArrivalInterval() * ALPHA));
						handlerServiceTimePerformance.setLastArrivalInterval(interval);
						handlerServiceTimePerformance.setMaxArrivalInterval(Math.max(handlerServiceTimePerformance.getMaxArrivalInterval(), interval));
						handlerServiceTimePerformance.setMinArrivalInterval(Math.min(handlerServiceTimePerformance.getMinArrivalInterval(), interval));

						handlerServiceTimePerformance.setLastArrival(start);
					}
					handlerServiceTimePerformance.setCount(handlerServiceTimePerformance.getCount() + 1);
				} else
				{
					handlerServiceTimePerformance = new HandlerServiceTimePerformance(
							pId,
							duration,
							duration,
							duration,
							duration,
							0,
							start,
							0,
							start,
							start, 1
					);
				}
				handlerServiceTimePerformanceRepository.save(handlerServiceTimePerformance);
			} finally
			{
				lock.unlock();
			}
		}
	}


	public void saveInvocationsPerformance(String bundle, String component, String action, HashMap<String, Integer> invocations) throws NoSuchAlgorithmException {
		var pId = "ic__" + bundle + "_" + component + "_" + action;
		var lock = lockRegistry.obtain(pId);
		if (lock.tryLock())
		{
			try
			{
				var hId = Handler.generateId(bundle, component, action);
				var handler = handlerRepository.findById(hId).orElseThrow();
				for (String payload : handler.getInvocations().values().stream().map(Payload::getName).distinct().toList())
				{
					var id = bundle + "_" + component + "_" + action + '_' + payload;
					var hip = handlerInvocationCountPerformanceRepository.findById(id).orElseGet(()
							-> {
						var hi = new HandlerInvocationCountPerformance();
						hi.setId(id);
						hi.setLastCount(0);
						hi.setMeanProbability(0);
						return handlerInvocationCountPerformanceRepository.save(hi);
					});
					hip.setLastCount(invocations.getOrDefault(payload, 0));
					hip.setMeanProbability(((1 - ALPHA) * hip.getMeanProbability()) + (ALPHA * invocations.getOrDefault(payload, 0)));
					handlerInvocationCountPerformanceRepository.save(hip);
				}

			} finally
			{
				lock.unlock();
			}
		}
	}


	public Double getInvocationProbability(String bundle, String component, String action, String payload) {
		return handlerInvocationCountPerformanceRepository.findById(bundle + "_" + component + "_" + action + "_" + payload).map(
				HandlerInvocationCountPerformance::getMeanProbability
		).orElse(1.0);
	}

	@Override
	public void sendServiceTimeMetricMessage(PerformanceServiceTimeMessage message) throws Exception {
		saveServiceTimePerformance(
				message.getBundle(),
				message.getComponent(),
				message.getAction(),
				message.getStart(),
				message.getEnd()
		);
	}

	@Override
	public void sendInvocationMetricMessage(PerformanceInvocationsMessage message) throws Exception {
		saveInvocationsPerformance(message.getBundle(),
				message.getComponent(),
				message.getAction(),
				message.getInvocations());
	}
}
