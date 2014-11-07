package com.greenisland.taxi.test;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.Socket;

public class Task implements Runnable {

	private Socket socket;

	public Task(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		try {
			handleSocket();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void handleSocket() throws Exception {
		Reader reader = new InputStreamReader(socket.getInputStream(), "GBK");
		char[] chars = new char[64];
		int len;
		StringBuilder sb = new StringBuilder();
		String temp;
		int index;
		while ((len = reader.read(chars)) != -1) {
			temp = new String(chars, 0, len);
			if ((index = temp.indexOf("eof")) != -1) {
				sb.append(temp.substring(0, index));
				break;
			}
			sb.append(temp);
		}
		System.out.println("from client: " + sb);
		Writer writer = new OutputStreamWriter(socket.getOutputStream(), "GBK");
		writer.write("Hello client!");
		writer.write("eof");
		writer.flush();
		reader.close();
		socket.close();
	}
}
