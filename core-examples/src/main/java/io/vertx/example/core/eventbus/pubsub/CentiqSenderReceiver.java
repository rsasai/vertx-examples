package io.vertx.example.core.eventbus.pubsub;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
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
		String eb_addr = "addr-for-commands";

		// sender role
		vertx.setPeriodic(
				getRandom(20000, 5000),
				v -> eb.publish(eb_addr,
						String.format("%s: %s %s %s",
								getObjNameForHuman(this),
								"\"Change schedule of memory agent to every",
								getRandom(600, 10),
								"seconds\"")));

		// receiver role
		eb.consumer(eb_addr, message -> {
			if ( isMyCommand(message) ) {
				// my publication; ignore
				System.out.println("... ignoring my own command ...");
				return;
			}
			System.out.println("Received command from " + message.body());	
		});

		// introduce self
		System.out.printf( "I am host-\"%s\"\n", getObjNameForHuman(this) );	
	}

	private boolean isMyCommand(Message<Object> message) {
		return message.body().toString().startsWith( getObjNameForHuman(this) );
	}

	private String getObjNameForHuman(Object obj) {
		return "host-" + obj.toString().replaceAll("^.*@", "").substring(0, 3);
	}

	private int getRandom(int max, int min) {
		return (int)(Math.random() * ((max - min) + 1)) + min;
	}
}
