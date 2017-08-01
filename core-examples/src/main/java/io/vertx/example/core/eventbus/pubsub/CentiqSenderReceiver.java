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

	private static final String MODE = "demo";				// demo, detail, etc.

	@Override
	public void start() throws Exception {

		EventBus eb = vertx.eventBus();
		String eb_addr = "addr-for-commands";

		// sender role
		vertx.setPeriodic(
				CentiqUtil.getRandom(20000, 5000),
				v -> {
					String msg = CentiqMessage.createPubMessage(MODE, this);
					CentiqUtil.clearConsole();
					System.out.println(ANSI_RED + CentiqMessage.getPubMessageConsole(MODE, msg) + ANSI_RESET);
					eb.publish(eb_addr, msg);
				});

		// receiver role
		eb.consumer(eb_addr, message -> {
			if ( CentiqMessage.isMyCommand(message, this) ) {
				// my publication; ignore
//				System.out.println("... ignoring my own command ...");
				return;
			}
			CentiqUtil.clearConsole();
			System.out.println(CentiqMessage.getSubMessageConsole(MODE, message.body().toString()));
		});

		// introduce self
		System.out.printf( "I am host-\"%s\"\n", CentiqMessage.getObjNameForHuman(this) );
	}
}

/**
 * Centiq Message class
 * @author rsasai
 * @since  1 Aug 2017
 */
class CentiqMessage {

	static String createPubMessage(String mode, Object obj) {
		switch (mode) {
			case "demo":
				return String.format("%s: %s %s",
						getObjNameForHuman(obj),
						"random number",
						CentiqUtil.getRandom(1000, 100));
			case "detail":
				return String.format("%s: %s %s %s",
						getObjNameForHuman(obj),
						"\"Change schedule of memory agent to every",
						CentiqUtil.getRandom(600, 10),
						"seconds\"");
			default:
				throw new IllegalArgumentException("no such message mode");
		}
	}

	static String getPubMessageConsole(String mode, String msg) {
		switch (mode) {
			case "demo":
				return msg.replaceFirst("host.*: ", "Publishing ");
			case "detail":
				return msg;
			default:
				throw new IllegalArgumentException("no such message mode");
		}
	}

	static String getSubMessageConsole(String mode, String msg) {
		switch (mode) {
			case "demo":
				return "Received: " + msg.replaceFirst("host.*: random number ", "");
			case "detail":
				return "Received: " + msg;
			default:
				throw new IllegalArgumentException("no such message mode");
		}
	}

	static boolean isMyCommand(Message<Object> message, Object obj) {
		return message.body().toString().startsWith( getObjNameForHuman(obj) );
	}

	static String getObjNameForHuman(Object obj) {
		return "host-" + obj.toString().replaceAll("^.*@", "").substring(0, 3);
	}
}

/**
 * Centiq Utility class
 * @author rsasai
 * @since  1 Aug 2017
 */
class CentiqUtil {

	public static void clearConsole() {
		System.out.println( "\033[2J");		// clear the screen
		System.out.println( "\033[0;0H");	// jump to 0,0
	}

	public static int getRandom(int max, int min) {
		return (int)(Math.random() * ((max - min) + 1)) + min;
	}
}
