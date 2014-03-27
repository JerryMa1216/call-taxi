package com.greenisland.taxi.test;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	public static void main(String[] args) throws Exception{
		ServerSocket socketServer = new ServerSocket(9090);
		while(true){
			Socket socket = socketServer.accept();
			InputStream in = socket.getInputStream();
			byte[] b = new byte[7];
			int len;
			try {
				while((len= in.read(b))!= -1){
					String value = new String(b,0,len);
					double d = Double.parseDouble(value);
					System.out.println(d);
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}
}

class Client {
	static class SendThread extends Thread{
		Socket socket;
		byte[] value;
		public SendThread (Socket socket,String value){
			super();
			this.socket = socket;
			this.value = value.getBytes();
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			while(true){
				try {
					OutputStream out = socket.getOutputStream();
					out.write(value);
					out.flush();
					Thread.sleep(1000);
				} catch (Exception e) {
					return;
				}
			}
		}
		
		
	}
	public static void main(String[] args) throws Exception{
		Socket server = new Socket("localhost",9090);
		SendThread send1 = new SendThread(server, "1111.11");
		SendThread send2 = new SendThread(server, "2222.22");
		SendThread send3 = new SendThread(server, "3333.33");
		SendThread send4 = new SendThread(server, "4444.44");
		send1.start();
		send2.start();
		send3.start();
		send4.start();
	}
}