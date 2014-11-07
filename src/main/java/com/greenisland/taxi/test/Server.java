package com.greenisland.taxi.test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {
		int port = 8888;
		ServerSocket server = new ServerSocket(port);
		while (true) {
			Socket socket = server.accept();
			new Thread(new Task(socket)).start();
		}
	}

}
