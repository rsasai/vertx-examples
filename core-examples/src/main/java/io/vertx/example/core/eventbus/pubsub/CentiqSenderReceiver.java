package io.vertx.example.core.eventbus.pubsub;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.example.util.Runner;

/**
 * @author rsasai
 * @since  27.07.17
 */
public class CentiqSenderReceiver extends AbstractVerticle {

	// Convenience method so you can run it in your IDE
	public static void main(String[] args) {
		Runner.runClusteredExample(CentiqSenderReceiver.class);
	}

	@Override
	public void start() throws Exception {

		EventBus eb = vertx.eventBus();

		// sender role
		vertx.setPeriodic(
				(int)(Math.random() * ((20000 - 5000) + 1)) + 5000,
				v -> eb.publish("news-feed",
						String.format("%s: %s %s %s",
								getObjNameForHuman(this),
								"\"Change schedule of memory agent to every",
								(int)(Math.random() * ((600 - 10) + 1)) + 10,
								"seconds\"")));

		// receiver role
		eb.consumer("news-feed", message -> {
			if ( message.body().toString().startsWith( getObjNameForHuman(this) ) ) {
				// my publication; ignore
				System.out.println("...");	
				return;
			}
			System.out.println("Received command from " + message.body());	
		});

		// introduce self
		System.out.printf( "I am host-\"%s\"\n", getObjNameForHuman(this) );	
	}

	private String getObjNameForHuman(Object obj) {
		return "host-" + obj.toString().replaceAll("^.*@", "").substring(0, 3);
	}
}
