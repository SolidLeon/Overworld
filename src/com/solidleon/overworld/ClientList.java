package com.solidleon.overworld;

import java.util.ArrayList;
import java.util.List;

public class ClientList {

	private static ClientList instance = new ClientList();
	
	private List<ClientThread> clients = new ArrayList<>();
	
	public static ClientList getInstance() {
		return instance;
	}
	
	private ClientList(){
		
	}

	public synchronized void add(ClientThread clientThread) {
		clients.add(clientThread);
	}

	public synchronized void remove(ClientThread clientThread) {
		clients.remove(clientThread);
	}
	

}
