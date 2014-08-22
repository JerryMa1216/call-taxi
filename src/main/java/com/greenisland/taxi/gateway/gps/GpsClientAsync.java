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

import com.bstek.dorado.core.Configure;
import com.greenisland.taxi.common.constant.GPSCommand;

public class GpsClientAsync extends Thread {
	private static Logger log = Logger.getLogger(GpsClientAsync.class.getName());
	private Socket socket;
	private SyncResponse syncResponse;
	private boolean isRunning = false;
	private String host;
	private int port;
	private String username;
	private String password;
	private Writer writer;
	private Reader reader;

	public GpsClientAsync(SyncResponse syncResponse) {
		super();
		this.syncResponse = syncResponse;
		initAsync();
		this.start();
	}

	private void initAsync() {
		try {
			if (getSocket() != null && getSocket().isConnected() && !getSocket().isClosed()) {
				try {
					getSocket().shutdownInput();
					getSocket().close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			setHost(Configure.getString("host"));
			setPort(Integer.parseInt(Configure.getString("port")));
			setUsername(Configure.getString("username"));
			setPassword(Configure.getString("password"));
			setSocket(new Socket(getHost(), getPort()));
			setWriter(new OutputStreamWriter(getSocket().getOutputStream(), "GBK"));
			setReader(new InputStreamReader(getSocket().getInputStream(), "GBK"));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean sendMessage(String message) {
		try {
			writer.write(message);
			writer.flush();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public void cancel() {
		isRunning = true;
		if (getSocket() != null && getSocket().isConnected() && !getSocket().isClosed()) {
			try {
				writer.close();
				reader.close();
				getSocket().shutdownInput();
				getSocket().close();
				setSocket(null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void run() {
		super.run();
		try {
			char chars[] = new char[64];
			int len = 0;
			StringBuffer sb;
			while (!isRunning) {
				sb = new StringBuffer();
				log.info("==========系统后台监控接口");
				while ((len = reader.read(chars)) != -1) {
					sb.append(new String(chars, 0, len));
					if (sb.indexOf(">>") != -1) {
						break;
					}
				}
				if(len > 0){
					log.info("叫车请求响应===========");
					String msg1 = sb.substring(2);
					String msg2 = msg1.substring(0, msg1.indexOf(">"));
					// 消息id
					String msgId = msg2.substring(0, 4);
					if (msgId.equals(Integer.toString(GPSCommand.GPS_TAXI_RESP))) {
						log.info("====== 召车抢答响应 ======");
						syncResponse.handlerResponse(sb.toString(), this);
					} else if (msgId.equals(Integer.toString(GPSCommand.GPS_CALL_RESP))) {
						log.info("======= 召车响应 ======");
						log.info(sb.toString());
						log.info("======= 召车响应 ======");
					}
				}
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public Writer getWriter() {
		return writer;
	}

	public void setWriter(Writer writer) {
		this.writer = writer;
	}

	public Reader getReader() {
		return reader;
	}

	public void setReader(Reader reader) {
		this.reader = reader;
	}

}
