package io.github.axway.apimplus.websocket.client;

import com.beust.jcommander.Parameter;

class Config {
	@Parameter(names = { "-h", "--help" }, description = "Show help screen.", help = true, order = 1)
	private boolean help = false;

	@Parameter(names = "--url", description = "Target WebSocket endpoint", order=2)
	private String url = "ws://localhost:8025/echo";

	@Parameter(names = "--endpoints", description = "Number of parallel client endpoints", order=3)
	private int endpointCount = 1;

	@Parameter(names = "--messages", description = "Number of messages to be send to the server", order=4)
	private int messageCount = 1;

	@Parameter(names = "--delay", description = "Delay between sending messages (milliseconds)", order=5)
	private long delayMillis = 1000;
	
	@Parameter(names = "--rampup", description = "Delay between ramp up a new client endpoint (milliseconds)", order=6) 
	private long delayRampupMillis = 0;
	
	@Parameter(names = "--timeout", description = "", order=7)
	private long timeoutMillis = 60000;


	public String getUrl() {
		return this.url;
	}

	public int getEndpointCount() {
		return this.endpointCount;
	}

	public int getMessageCount() {
		return this.messageCount;
	}

	public long getDelayMillis() {
		return this.delayMillis;
	}
	
	public long getDelayRampupMillis() {
		return this.delayRampupMillis;
	}
	
	public long getTimeoutMillis() {
		return this.timeoutMillis;
	}

	public boolean isHelp() {
		return this.help;
	}
}
