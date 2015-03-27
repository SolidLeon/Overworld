package com.solidleon.overworld.main;

import com.solidleon.overworld.Logging;
import com.solidleon.overworld.Server;


public class Main {

	public static void main(String[] args) {
		Logging.openLogWindow();
		Thread.setDefaultUncaughtExceptionHandler((t, e) -> (Logging.exception(e)));
		int port = 12345;
		if (args.length > 0) {
			Logging.info("Get command-line arguments ...");
			
			try {
				Logging.info("Parse port '%s'", args[0]);
				port = Integer.parseInt(args[0]);
			} catch (Exception ex) {
				Logging.exception(ex, "Error parsing port '%s'", args[0]);
				System.exit(1);
			}
		}
		Logging.info("Start server thread ...");
		new Thread(new Server(port)).start();
	}

}
