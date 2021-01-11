package io.github.axway.apimplus.websocket.client;

import com.beust.jcommander.Parameter;

class Config {
	@Parameter(names = { "-h", "--help" }, description = "Show help screen.", help = true, order = 1)
	private boolean help = false;

	@Parameter(names = "--url", description = "Target WebSocket endpoint", order=2)
	private String url = "ws://echo.websocket.org";

	@Parameter(names = "--endpoints", description = "Number of parallel client endpoints", order=3)
	private int endpointCount = 1;

	@Parameter(names = "--messages", description = "Number of messages to be send to the server", order=4)
	private int messageCount = 1;

	@Parameter(names = "--delay", description = "Delay between sending messages (milliseconds)", order=5)
	private long delayMillis = 1000;


	public String getUrl() {
		return url;
	}

	public int getEndpointCount() {
		return endpointCount;
	}

	public int getMessageCount() {
		return messageCount;
	}

	public long getDelayMillis() {
		return delayMillis;
	}

	public boolean isHelp() {
		return help;
	}
}
