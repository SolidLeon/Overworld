package com.solidleon.overworld;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientThread implements Runnable {

	private Socket socket;
	private Thread thread;
	private boolean running;

	private StringBuilder sbData = new StringBuilder();
	private StringBuilder sbPrint = new StringBuilder();
	
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
				String line = readLine(in);
				
				if ("exit".equals(line)) {
					send(out, "Bye!");
					break;
				}
				
			}
		} catch (Exception ex) {
			System.err.println("!!! Exception in client thread. Disconnecting client ...");
			ex.printStackTrace();
		} finally {
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
	
	private String readLine(BufferedReader in) throws IOException {
		String line = in.readLine();
		System.out.println("RECV  '" + socket.getInetAddress().getHostAddress() + "'  '" + line + "'");
		for (String logLine :  toHexString(line)) {
			System.out.println(logLine);
		}
		return line;
	}

	private void send(PrintWriter out, String format, Object ...args) {
		String msg = String.format(format, args);
		System.out.println("SEND  '" + socket.getInetAddress().getHostAddress() + "'  '" + msg + "'");
		for (String logLine :  toHexString(msg)) {
			System.out.println(logLine);
		}
		out.println(msg);
		out.flush();
	}
	
	// 80 Characters width
	// 8 characters indentation
	// 3 spaces between hex and characters
	// 46 characters for data
	// 23 characters for characters
	//DATA+CHARACTERS = 70
	//........[DATA]...[CHARACTERS]
	private List<String> toHexString(String str) {
		List<String> lines = new ArrayList<>();
		byte[] data = str.getBytes();
		
		sbData.setLength(0);
		sbPrint.setLength(0);
		lines.add("    MSG (len=" + data.length + ")");
		for (int i = 0; i < data.length; i++) {
			if (i % 23 == 0) {
				if (sbData.length() > 0) {
					String line = "        " + sbData.toString() + "   " + sbPrint.toString();
					lines.add(line);
					sbData.setLength(0);
					sbPrint.setLength(0);
				}
			}
			sbData.append(String.format("%02X", data[i]));
			sbPrint.append(Character.isISOControl(data[i]) ? '.' : (char) data[i]);
		}
		if (sbData.length() > 0) {
			String line = "        " + sbData.toString() + "   " + sbPrint.toString();
			lines.add(line);
		}
		return lines;
	}
}
