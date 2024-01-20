package org.evento.demo.agent;

import io.sentry.Sentry;
import io.sentry.protocol.User;
import org.evento.application.EventoBundle;
import org.evento.demo.agent.agents.DemoLifecycleAgent;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Random;

@Component
public class AgentDispatcher implements CommandLineRunner {


	private final EventoBundle eventoBundle;

	public AgentDispatcher(EventoBundle eventoBundle) {
		this.eventoBundle = eventoBundle;
	}

	@Override
	public void run(String... args) throws Exception {


		var demoLifecycleAgent = eventoBundle.getInvoker(DemoLifecycleAgent.class);

		Thread.sleep(3000);


		for (int i = 0; i < 10; i++)
		{
			Thread.sleep(500);
			int finalI = i;
			new Thread(() -> {
				try
				{

					var user = new User();
					if(new Random().nextBoolean()) {
						user.setEmail("gabor.galazzo@gmail.com");
						user.setName("Gabor Galazzo");
						user.setId("123456");
						user.setUsername("gaborando");
						user.setData(Map.of("isAdmin", "true"));
					}else{
						user.setEmail("cenaturalmente@gmail.com");
						user.setName("Mariann Szilagyi");
						user.setId("654321");
						user.setUsername("cenaturalmente");
						user.setData(Map.of("isAdmin", "false"));
					}
					Sentry.setUser(user);
					demoLifecycleAgent.action(finalI);
					System.out.println("--------------------");
					System.out.println(Sentry.getLastEventId());
				} catch (Exception e)
				{
					e.printStackTrace();
				}
				System.out.println(finalI + "_end");
			}).start();
		}
		// eventoBundle.gracefulShutdown();

		/*
		var listStart = IntStream.range(0, 10).parallel().mapToObj(demoLifecycleAgent::action).toList();
		System.out.println("listStart (10) MeanCreateTime: "+listStart.stream().filter(Report::success).mapToLong(Report::createTime).average().orElse(0));
		System.out.println("listStart (10) MeanMeanUpdateTime: "+listStart.stream().filter(Report::success).mapToLong(Report::meanUpdateTime).average().orElse(0));
		System.out.println("listStart (10) MeanDeleteTime: "+listStart.stream().filter(Report::success).mapToLong(Report::deleteTime).average().orElse(0));
		listStart.stream().filter(r -> !r.success()).forEach(System.out::println);*/

		/*
		var listMiddle = IntStream.range(0, 30).mapToObj(demoLifecycleAgent::action).toList();
		var listEnd = IntStream.range(0, 10).mapToObj(demoLifecycleAgent::action).toList();
		System.out.println("listMiddle (30) MeanCreateTime: "+listMiddle.stream().filter(Report::success).mapToLong(Report::createTime).average().orElse(0));
		System.out.println("listMiddle (30) MeanMeanUpdateTime: "+listMiddle.stream().filter(Report::success).mapToLong(Report::meanUpdateTime).average().orElse(0));
		System.out.println("listMiddle (30) MeanDeleteTime: "+listMiddle.stream().filter(Report::success).mapToLong(Report::deleteTime).average().orElse(0));
		System.out.println("listEnd (10) MeanCreateTime: "+listEnd.stream().filter(Report::success).mapToLong(Report::createTime).average().orElse(0));
		System.out.println("listEnd (10) MeanMeanUpdateTime: "+listEnd.stream().filter(Report::success).mapToLong(Report::meanUpdateTime).average().orElse(0));
		System.out.println("listEnd (10) MeanDeleteTime: "+listEnd.stream().filter(Report::success).mapToLong(Report::deleteTime).average().orElse(0));

		listMiddle.stream().filter(r -> !r.success()).forEach(System.out::println);
		listEnd.stream().filter(r -> !r.success()).forEach(System.out::println);

		//eventoApplication.shutdown();
		*/
		// eventoBundle.gracefulShutdown();

	}
}
