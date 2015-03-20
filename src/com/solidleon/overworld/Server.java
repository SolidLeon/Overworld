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
		try (ServerSocket server = new ServerSocket(port)) {
			System.out.println("Server listening on port " + port);
			while (running) {
				Socket clientSocket = null;
				try {
					clientSocket = server.accept();
					new ClientThread(clientSocket).start();
				} catch (Exception ex) {
					System.err.println("Error creating client ...");
					if (clientSocket != null) {
						System.err.println("Closing socket ...");
						try {
							clientSocket.close();
						} catch (IOException ex2) {
							ex2.printStackTrace();
						}
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}
