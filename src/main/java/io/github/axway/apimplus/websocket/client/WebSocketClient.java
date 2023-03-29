package io.github.axway.apimplus.websocket.client;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.websocket.ContainerProvider;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.WebSocketContainer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

public class WebSocketClient implements AutoCloseable {

	private static final Logger log = LogManager.getLogger(WebSocketClient.class);

	private final Config config;
	private final WebSocketContainer container;
	private final List<Endpoint> endpoints = new ArrayList<Endpoint>();
	private final URI uri;

	public static void main(String[] args) throws Exception, IOException {
		int rc = 0;
		Config cfg = parseArgs(args);

		try (WebSocketClient client = new WebSocketClient(cfg)) {
			client.open();

			for (int m = 0; m < cfg.getMessageCount(); m++) {
				client.broadcast("hello (#" + m + ")");
				Thread.sleep(cfg.getDelayMillis());
			}

			client.waitForCompletion();

			if (!client.checkMessages()) {
				rc = 1;
			}
		} catch (Exception e) {
			log.error("error occurred", e);
			rc = 2;
		}

		System.exit(rc);
	}

	private static Config parseArgs(String[] args) {
		Config cfg = new Config();

		// Parse command line
		try {
			JCommander jc = JCommander.newBuilder().addObject(cfg).programName("ws-client-simple").build();
			jc.parse(args);
			if (cfg.isHelp()) {
				printHelpAndExit(jc);
			}
		} catch (ParameterException e) {
			log.error(e.getLocalizedMessage());
			System.exit(1);
		}

		return cfg;
	}

	private static void printHelpAndExit(JCommander jc) {
		jc.usage();
		System.exit(1);
	}

	public WebSocketClient(Config cfg) throws DeploymentException, IOException, InterruptedException {
		this.config = Objects.requireNonNull(cfg);
		this.container = ContainerProvider.getWebSocketContainer();
		this.uri = URI.create(this.config.getUrl());
	}

	public final void open() throws DeploymentException, IOException, InterruptedException {
		log.info("Connect to URI: " + this.uri);
		// create endpoints
		for (int i = 0; i < this.config.getEndpointCount(); i++) {
			this.endpoints.add(new Endpoint(this.container, this.uri));
			if (this.config.getDelayRampupMillis() > 0 && i < (this.config.getEndpointCount() - 1)) {
				Thread.sleep(this.config.getDelayRampupMillis());
			}
		}

		// check endpoints
		for (Endpoint ep : this.endpoints) {
			if (!ep.isOpen()) {
				close();
				throw new IOException("couldn't open all endpoints");
			}
		}
		log.debug("all endpoint available");
	}

	public void broadcast(String text) throws IOException {
		for (Endpoint ep : this.endpoints) {
			ep.sendText(text);
		}
	}

	public void waitForCompletion() throws IOException, InterruptedException {
		long untilTimeMillis = System.currentTimeMillis() + this.config.getTimeoutMillis();
		boolean completed = false;

		while (!completed && System.currentTimeMillis() < untilTimeMillis) {
			completed = true;
			Thread.sleep(500);

			for (Endpoint ep : this.endpoints) {
				int missingMessageCount = ep.getMissingMessageCount();
				if (missingMessageCount > 0) {
					completed = false;
					break;
				}
			}
		}

		if (!completed) {
			log.error("wait for completion canceled after timeout of " + this.config.getTimeoutMillis()
					+ " milliseconds");
		}
	}

	public boolean checkMessages() {
		boolean succeeded = true;
		for (Endpoint ep : this.endpoints) {
			int missingMessageCount = ep.getMissingMessageCount();
			if (missingMessageCount != 0) {
				succeeded = false;
				log.error(missingMessageCount + " messages are missing for endpoint [" + ep.getId() + "]");
			}
		}

		return succeeded;
	}

	@Override
	public void close() throws IOException {
		this.endpoints.forEach(ep -> {
			try {
				if (ep.isOpen())
					ep.close();
			} catch (Exception e) {
				log.error("failed to close endpoint", e);
			}
		});
		log.info("all endpoints closed");
	}
}
