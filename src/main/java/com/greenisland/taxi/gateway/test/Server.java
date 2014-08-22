package com.greenisland.taxi.gateway.test;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	public static void main(String[] args) throws Exception {
		int port = 8899;
		ServerSocket server = new ServerSocket(port);
		Socket socket = server.accept();
		Reader reader = new InputStreamReader(socket.getInputStream());
		char chars[] = new char[64];
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
		Writer writer = new OutputStreamWriter(socket.getOutputStream());
		writer.write("你好服务端");
		writer.flush();
		writer.close();
		reader.close();
		socket.close();
		server.close();
	}
}
