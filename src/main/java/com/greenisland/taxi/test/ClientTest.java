package com.greenisland.taxi.test;

import java.net.ServerSocket;
import java.net.Socket;

/**
 * 
 * @author Jerry
 * @E-mail jerry.ma@bstek.com
 * @version 2013-10-24下午11:30:37
 */
public class ClientTest {
	private ServerSocket serverSocket;
	private int listenPort;
	private Thread listenerThread;

	public ClientTest(int listenPort) {
		this.listenPort = listenPort;
	}

	public void runIt() throws Exception {

		this.serverSocket = new ServerSocket(listenPort);
		this.listenerThread = new Thread() {
			public void run() {
				synchronized (ClientTest.this) {
					ClientTest.this.notify();
				}
				try {
					Socket clientSocket = ClientTest.this.serverSocket.accept();
					receiveDataOnServer(clientSocket);
					clientSocket.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		};
		this.listenerThread.start();
		synchronized (this) {
			this.wait();
		}
		Socket sock = new Socket("localhost", listenPort);
		sendDataFromClient(sock);
		receiveDataFromServer(sock);
		System.out.println(">> All done!");
		listenerThread.join();
		System.out.println(">> Bye Bye!");
	}

	private void sendDataFromClient(Socket sock) throws Exception {
		sock.getOutputStream().write("Hello".getBytes());
		System.out.println(">> Sent HELLO");
	}

	private void receiveDataFromServer(Socket sock) throws Exception {
		byte[] buf = new byte[1024];
		int bread = sock.getInputStream().read(buf);
		String response = new String(buf, 0, bread);
		System.out.println("<< RESPONSE from SERVER: " + response);
	}

	private void receiveDataOnServer(Socket sock) throws Exception {

		byte[] buf = new byte[1024];
		int bread = sock.getInputStream().read(buf);
		String response = new String(buf, 0, bread);
		System.out.println(">> Received on Server : " + response);
		sock.getOutputStream().write("WORLD".getBytes());
		System.out.println(">> SERVER sent response back");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		ClientTest c = new ClientTest(1234);
		c.runIt();

	}
}
