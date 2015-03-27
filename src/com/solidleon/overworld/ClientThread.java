package com.solidleon.overworld;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ClientThread implements Runnable {

	private String accountName;
	
	private Socket socket;
	private Thread thread;
	private boolean running;

	private BufferedReader in;
	private PrintWriter out;
	
	private StringBuilder sbData = new StringBuilder();
	private StringBuilder sbPrint = new StringBuilder();
	
	private Map<String, String> properties = new HashMap<>();
	
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
			Logging.info("New client thread started");
			Logging.info("Client connection from '%s'", socket.getInetAddress().getHostAddress());

			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream());

			ScriptProcessor.getInstance().executeFunction(this, "scripts/commands.js", "onConnect");
			
			while (running) {
				String line = readLine();
				if (line == null) {
					Logging.info("Remote '%s' closed connection!", socket.getInetAddress().getHostAddress());
					break;
				}
				
				ScriptProcessor.getInstance().executeProcess(this, "scripts/commands.js", line);
				
			}
		} catch (Exception ex) {
			Logging.exception(ex, "!!! Exception in client thread. Disconnecting client '%s' ...", socket.getInetAddress().getHostAddress());
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					Logging.exception(e);
				}
				ClientList.getInstance().remove(this);
			}
		}
	}
	
	public String readLine() throws IOException {
		String line = in.readLine();
		if (line != null) {
			Logging.info("RECV from '%s':  '%s'", socket.getInetAddress().getHostAddress(), line);
			for (String logLine :  toHexString(line)) {
				Logging.info(logLine);
			}
		}
		return line;
	}

	public void send(String format, Object ...args) {
		String msg = String.format(format, args);
		Logging.info("SEND to '%s':  '%s'", socket.getInetAddress().getHostAddress(), msg);
		for (String logLine :  toHexString(msg)) {
			Logging.info(logLine);
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
	
	public void disconnect() {
		running = false;
	}
	
	public void writeProperty(String key, String value) {
		properties.put(key, value);
		if (accountName != null) {
			File saveDirFile = new File("saves");
			if (!saveDirFile.exists()) {
				Logging.info("Create save directory '%s' ...", saveDirFile.getAbsolutePath());
				saveDirFile.mkdirs();
			}
			File saveFile = new File(saveDirFile, accountName + ".txt");
			List<String> write = new ArrayList<>();
			for (Entry<String, String> entry : properties.entrySet()) {
				write.add(entry.getKey() + "=" + entry.getValue());
			}
			try {
				Logging.info("Write properties to '%s' ...", saveFile.getAbsolutePath());
				Files.write(saveFile.toPath(), write, Charset.defaultCharset());
			} catch (IOException e) {
				Logging.exception(e);
			}
		}
	}
	
	public String readProperty(String key) {
		return properties.get(key);
	}
	
	private void loadProperties() throws IOException {
		File loadAccountFile = new File("saves/" + accountName + ".txt");
		Logging.info("Load account from '%s'", loadAccountFile.getAbsolutePath());
		List<String> lines = Files.readAllLines(loadAccountFile.toPath(), Charset.defaultCharset());
		properties = new HashMap<>();
		for (String line : lines) {
			String key = line.substring(0, line.indexOf('='));
			String value = line.substring(line.indexOf('=') + 1);
			properties.put(key, value);
		}
	}
	
	public String getAccountName() {
		return accountName;
	}
	
	public void setAccountName(String accountName) {
		Logging.info("Set account name for '%s' to '%s' ...", this.accountName, accountName);
		this.accountName = accountName;
		try {
			loadProperties();
		} catch (IOException e) {
			Logging.exception(e, "Failed loading properties for '%s'", accountName);
			properties = new HashMap<>();
		}
	}
}
