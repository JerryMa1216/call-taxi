package com.greenisland.taxi.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;

public class Client {

	public static void main(String[] args) throws IOException {
		String host = "127.0.0.1";
		int port = 8888;
		Socket socket = new Socket(host, port);
		Writer writer = new OutputStreamWriter(socket.getOutputStream());
		writer.write("Hello server.");
		writer.write("eof");
		writer.flush();
		BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		socket.setSoTimeout(10 * 1000);
		char[] chars = new char[64];
		int len;
		StringBuffer sb = new StringBuffer();
		String temp;
		int index;
		try {
			while ((len = br.read(chars)) != -1) {
				temp = new String(chars, 0, len);
				if ((index = temp.indexOf("eof")) != -1) {
					sb.append(temp.substring(0, index));
					break;
				}
				sb.append(temp);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		System.out.println("from server : " + sb);
		writer.close();
		br.close();
		socket.close();
	}

}
