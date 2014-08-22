package com.greenisland.taxi.gateway.gps;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

public class GpsClientSync {
	private static Logger log = Logger.getLogger(GpsClientSync.class);
	private String host;
	private int port;
	private Socket socket;
	private Writer writer;
	private Reader reader;

	public GpsClientSync() {
		super();
	}

	public GpsClientSync(String host, int port) {
		super();
		this.host = host;
		this.port = port;
	}

	public Socket connect() {
		try {
			socket = new Socket(host, port);
			return socket;
		} catch (UnknownHostException e) {
			log.error(e.getMessage());
			e.printStackTrace();
			log.info("host地址不存在==========");
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public Writer getWriter() {
		try {
			writer = new OutputStreamWriter(socket.getOutputStream(), "GBK");
			return writer;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Reader getReader() {
		try {
			reader = new InputStreamReader(socket.getInputStream(), "GBK");
			return reader;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void cancel() {
		try {
			writer.close();
			reader.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		String host = "211.140.29.67";
		int port = 7743;
		Socket socket = new Socket(host, port);

		Writer writer = new OutputStreamWriter(socket.getOutputStream(), "GBK");
		writer.write("<<0001,0000000001,testuser,testpw>>");
		writer.flush();
		Reader reader = new InputStreamReader(socket.getInputStream(), "GBK");
		char chars[] = new char[64];
		int len;
		StringBuffer sb = new StringBuffer();
		while ((len = reader.read(chars)) != -1) {
			sb.append(new String(chars, 0, len));
			if (sb.indexOf(">>") != -1) {
				break;
			}
		}
		System.out.println(sb);
		writer.close();
		reader.close();
		socket.close();
	}

}
