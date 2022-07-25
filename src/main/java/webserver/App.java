package webserver;


import java.io.File;
import java.io.FileNotFoundException;
import webserver.handler.DocrootHandler;
import webserver.handler.ServerInfoHandler;
import webserver.server.WebServer;
import webserver.api.RequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import webserver.config.ConfigOptions;
import static webserver.config.ConfigOptions.getWorkdir;
import webserver.config.ConfigUtils;
import webserver.handler.TesHandler;

public class App {

	private static final Logger LOG = LoggerFactory.getLogger(App.class);
        public static ConfigUtils config;
        public static ConfigUtils cftes;
        public static ConfigOptions cfo;
        private static final String cfgName = "main.cfg";
        private static final String cfgPath = "config" + File.separator + cfgName;
	private App() {}

	/**
	 * Main application loop. Parses the command line args then tries to start a server instance.
	 *
	 * @param args String array of command line arguments
	 */
	public static void main(String[] args) {
                config = new ConfigUtils(new File(getWorkdir(3) + cfgPath), "config.json"){};
                config.load();
                cfo = new ConfigOptions();
               
                /*
                cftes = new ConfigUtils(new File(getWorkdir(3) + "config" + File.separator + "gg.cfg"), "tes.json"){};
                cftes.load(); */
		CommandLineOptions options = new CommandLineOptions(args);

		// If the help options is set, print usage and exit.
		if (options.help()) {
			printUsage();
			return;
		}

		// Otherwise read the CLI options (or defaults, as defined in CommandLineOptions)
		int port = options.port();
		int timeout = options.timeout();
		int maxThreads = options.maxThreads();
		String docroot = options.docroot();
		// Configure request handlers
		RequestHandler serverInfoHandler = new ServerInfoHandler(port, timeout, maxThreads, docroot, System.currentTimeMillis());
		RequestHandler docrootHandler = new DocrootHandler(docroot, Collections.singletonList(cfo.getPropertyString("indexFile")), cfo.getPropertyBoolean("directoryListing"));
                TesHandler TesHandler = new TesHandler("WP");
		List<RequestHandler> requestHandlers = Arrays.asList(serverInfoHandler, docrootHandler, TesHandler);
		// Create the server
		WebServer server = new WebServer(port, timeout, maxThreads, requestHandlers);
		// Register a shutdown hook to gracefully stop the server when the JVM is terminated
		Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHook(server)));
		// Start the server
		server.start();
	}

	/**
	 * Displays usage information
	 */
	// CHECKSTYLE:OFF - Allow use of System.out to display CLI usage
	@SuppressWarnings("squid:S106")
	private static void printUsage() {
		System.out.println("Java Web Server");
		System.out.println("---------------");
		System.out.println("\tA simple multi-threaded web server written in Java and implementing the HTTP/1.1 specification.\n");

		System.out.println("Usage");
		System.out.println("-----");
		System.out.println("\tjava -jar java-webserver.jar [options]\n\n");

		try {
			new CommandLineOptions().printHelpOn(System.out);
		} catch (IOException e) {
			LOG.error("Unable to display help/usage information", e);
		}
	}
	//CHECKSTYLE:ON

	/**
	 * The ShutdownHook is registered to run when the JVM is terminated so the server can be terminated gracefully and
	 * in-progress requests can be completed.
	 */
	private static class ShutdownHook implements Runnable {

		private static final Logger LOG = LoggerFactory.getLogger(ShutdownHook.class);

		private WebServer server;

		ShutdownHook(WebServer server) {
			this.server = server;
		}

		@Override
		public void run() {
			if (server.running()) {
				server.stop();
			} else {
				LOG.debug("Server not running, nothing to do.");
			}
		}
	}

}
