package com.solidleon.overworld.main;

import com.solidleon.overworld.Server;

public class Main {

	public static void main(String[] args) {
		new Thread(new Server(12345)).start();
	}

}
