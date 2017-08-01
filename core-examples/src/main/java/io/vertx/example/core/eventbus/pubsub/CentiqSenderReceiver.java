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

	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_RESET = "\u001B[0m";

	@Override
	public void start() throws Exception {

		EventBus eb = vertx.eventBus();
		String eb_addr = "addr-for-commands";

		// sender role
		vertx.setPeriodic(
				getRandom(20000, 5000),
				v -> {
					String msg = getMessage("demo");
//					String msg = getMessage("detail");
					eb.publish(eb_addr, msg);
					clearConsole();
					System.out.println(ANSI_RED + msg + ANSI_RESET);
				});

		// receiver role
		eb.consumer(eb_addr, message -> {
			if ( isMyCommand(message) ) {
				// my publication; ignore
//				System.out.println("... ignoring my own command ...");
				return;
			}
			clearConsole();
			System.out.println("Received: " + message.body());
		});

		// introduce self
		System.out.printf( "I am host-\"%s\"\n", getObjNameForHuman(this) );
	}

	private String getMessage(String mode) {
		switch (mode) {
			case "demo":
				return String.format("%s: %s %s",
						getObjNameForHuman(this),
						"random number",
						getRandom(1000, 100));
			case "detail":
				return String.format("%s: %s %s %s",
						getObjNameForHuman(this),
						"\"Change schedule of memory agent to every",
						getRandom(600, 10),
						"seconds\"");
			default:
				throw new IllegalArgumentException("no such message mode");
		}
	}

	private void clearConsole() {
	    System.out.println( "\033[2J");		// clear the screen
	    System.out.println( "\033[0;0H");	// jump to 0,0
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
