package io.github.axway.apimplus.websocket.client;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;

import javax.websocket.ClientEndpoint;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@ClientEndpoint
public class Endpoint implements AutoCloseable {

	private static Logger log = LogManager.getLogger(Endpoint.class);

	private static AtomicInteger count = new AtomicInteger(0);

	private final int id;
	private final Session session;

	private int msgCountReceived = 0;
	private int msgCountSent = 0;

	public Endpoint(WebSocketContainer container, URI uri) throws DeploymentException, IOException {
		this.id = Endpoint.count.incrementAndGet();
		this.session = container.connectToServer(this, uri);
	}

	public int getId() {
		return this.id;
	}

	public int getReceivedMessageCount() {
		return this.msgCountReceived;
	}

	public int getSentMessageCount() {
		return this.msgCountSent;
	}

	public int getMissingMessageCount() {
		return this.msgCountSent - this.msgCountReceived;
	}

	public void sendText(String text) throws IOException {
		log("send - " + text);
		this.session.getBasicRemote().sendText(text);
		this.msgCountSent++;
	}

	public boolean isOpen() {
		return this.session != null && this.session.isOpen();
	}

	@Override
	public void close() throws IOException {
		if (this.session != null && this.session.isOpen()) {
			this.session.close();
		}
	}

	@OnMessage
	public void onMessage(String message, Session session) {
		this.msgCountReceived++;
		log("received - " + message);
	}

	@OnOpen
	public void onConnection(Session session) throws IOException {
		log("connected");
	}

	@OnClose
	public void onClose() {
		log("disconnected - " + this.msgCountReceived + " message(s) received");
	}

	private void log(String message) {
		log.info("[" + this.id + "]: " + message);
	}
}
