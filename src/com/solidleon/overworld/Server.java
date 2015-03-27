package com.solidleon.overworld;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server implements Runnable {

	private int port;
	private boolean running;
	
	public Server(int port) {
		super();
		this.port = port;
	}

	@Override
	public void run() {
		running = true;
		Logging.info("Server thread started, bind socket to port '%d'...", port);
		try (ServerSocket server = new ServerSocket(port)) {
			Logging.info("Server listening on port %d", port);
			while (running) {
				Socket clientSocket = null;
				try {
					clientSocket = server.accept();
					new ClientThread(clientSocket).start();
				} catch (Exception ex) {
					Logging.error("Error creating client!");
					if (clientSocket != null) {
						Logging.error("Closing socket");
						try {
							clientSocket.close();
						} catch (IOException ex2) {
							ex2.printStackTrace();
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}
