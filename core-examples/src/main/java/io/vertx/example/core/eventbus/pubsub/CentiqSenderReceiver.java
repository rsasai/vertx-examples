package io.vertx.example.core.eventbus.pubsub;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.example.util.Runner;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

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

	private static final int mode = 1;			// 1: demo 2: detail
	private static final String MODE;
	static {
		switch (mode) {
			case 1:   MODE = CentiqMessage.DEMO;         break;
			case 2:   MODE = CentiqMessage.DETAIL;       break;
			default:  MODE = "bug";                      break;
		}
	}

	@Override
	public void start() throws Exception {

		EventBus eb = vertx.eventBus();
		String eb_addr = "addr-for-commands";

		// sender role
		vertx.setPeriodic(
				CentiqUtil.getRandom(20000, 5000),
				v -> {
					String msg = CentiqMessage.createPubMsg(MODE + "-create-pub-msg", this);
					CentiqUtil.clearConsole();
					System.out.println(
							ANSI_RED + CentiqMessage.getConsoleMsg(MODE + "-console-pub-msg", msg) + ANSI_RESET);
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
			System.out.println(CentiqMessage.getConsoleMsg(MODE + "-console-sub-msg", message.body().toString()));
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

	static final String DEMO       = "demo";
	static final String DETAIL     = "detail";

	private static final Map<String, Function<Object, String>> mapImpl;
	static {
		Map<String, Function<Object, String>> m = new HashMap<>();

		m.put(DEMO + "-create-pub-msg",
				obj -> String.format("%s: %s %s",
						getObjNameForHuman(obj),
						"random number",
						CentiqUtil.getRandom(1000, 100)) );
		m.put(DETAIL + "-create-pub-msg",
				obj -> String.format("%s: %s %s %s",
						getObjNameForHuman(obj),
						"\"Change schedule of memory agent to every",
						CentiqUtil.getRandom(600, 10),
						"seconds\"") );
		m.put(DEMO + "-console-pub-msg",
				msg -> msg.toString().replaceFirst("host.*: ", "Publishing ") );
		m.put(DETAIL + "-console-pub-msg", msg -> msg.toString() );
		m.put(DEMO + "-console-sub-msg",
				msg -> "Received: " + msg.toString().replaceFirst("host.*: random number ", "") );
		m.put(DETAIL + "-console-sub-msg", msg -> "Received: " + msg.toString() );

		mapImpl = Collections.unmodifiableMap(m);
	}

	static String createPubMsg(String command, Object obj) {
		checkArg(command);
		return mapImpl.get(command).apply(obj);
	}

	static String getConsoleMsg(String command, String msg) {
		checkArg(command);
		return mapImpl.get(command).apply(msg);
	}

	private static void checkArg(String command) {
		if (! mapImpl.containsKey(command)) {
			throw new IllegalArgumentException("Bad argument: " + command);
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
