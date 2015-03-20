package com.solidleon.overworld;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientThread implements Runnable {

	private Socket socket;
	private Thread thread;
	private boolean running;
	
	public ClientThread(Socket socket) {
		super();
		this.socket = socket;
	}

	public void start() {
		thread = new Thread(this);
		thread.start();
	}
	
	@Override
	public void run() {
		running = true;
		
		try {
			ClientList.getInstance().add(this);
			System.out.println("New client thread started");
			System.out.println("  Client connection from '" + socket.getInetAddress().getHostAddress() + "'");

			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter out = new PrintWriter(socket.getOutputStream());
			
			while (running) {
				String line = in.readLine();
				System.out.println("RECV  '" + socket.getInetAddress().getHostAddress() + "':  '" + line + "'");
			}
		} catch (Exception ex) {
			System.err.println("!!! Exception in client thread. Disconnecting client ...");
			ex.printStackTrace();
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ClientList.getInstance().remove(this);
			}
		}
	}

}
