package com.greenisland.taxi.gateway.gps;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.Socket;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.bstek.dorado.core.Configure;
import com.greenisland.taxi.common.constant.GPSCommand;
import com.greenisland.taxi.common.utils.TCPUtils;

@Component
public class GpsService {
	private static Logger log = Logger.getLogger(GpsService.class);
	@Resource(name = "syncResponse")
	private SyncResponse synResponse;
	private String host = Configure.getString("host");
	private int port = Integer.parseInt(Configure.getString("port"));
	private Writer writer;
	private Reader reader;
	private Socket socket;
	private GpsClientSync clientSync;
	private GpsClientAsync clientAsync;

	/**
	 * 初始化异步获取数据socket连接
	 */
	public void initAsync() {
		this.clientAsync = new GpsClientAsync(synResponse);
		this.socket = clientAsync.getSocket();
		this.writer = clientAsync.getWriter();
		this.reader = clientAsync.getReader();
	}

	/**
	 * 初始化同步获取数据socket连接
	 */
	public void initSync() {
		this.clientSync = new GpsClientSync(host, port);
		this.socket = clientSync.connect();
		this.reader = clientSync.getReader();
		this.writer = clientSync.getWriter();
	}

	private boolean login() {
		String loginMessage = TCPUtils.getLoginMsg(Configure.getString("username"), Configure.getString("password"));
		try {
			writer.write(loginMessage);
			writer.flush();
			char chars[] = new char[64];
			int len;
			// socket.setSoTimeout(5 * 1000);
			StringBuffer sb = new StringBuffer();
			while ((len = reader.read(chars)) != -1) {
				sb.append(new String(chars, 0, len));
				if (sb.indexOf(">>") != -1) {
					break;
				}
			}
			if (sb != null && sb.toString().indexOf("ER") != -1) {
				log.info("登录失败==========");
				return false;
			} else {
				log.info("登录成功==========");
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 发送叫车请求
	 * 
	 * @param message
	 */
	public void sendCallMessage(String message) {
		initAsync();
		boolean loginFlag;
		loginFlag = login();
		int loginCount = 0;
		while (!loginFlag) {
			if (loginCount < 3) {
				loginFlag = login();
				loginCount++;
			} else {
				log.info("登录失败，发送失败==========");
				this.clientAsync.cancel();
			}
		}
		clientAsync.sendMessage(message);
	}

	/**
	 * 发送非打车请求的所有请求
	 * 
	 * @param message
	 * @return
	 */
	public String sendMessage(String message) {
		initSync();
		String sendType = message.substring(2, 6);
		if (sendType.equals(GPSCommand.GPS_LOGIN_REQUEST)) {
			log.info("登录GPS服务器==========");
		} else if (sendType.equals(GPSCommand.GPS_AROUND_TAXIS_REQUEST)) {
			log.info("周边车辆查询==========");
		} else if (sendType.equals(GPSCommand.GPS_TAXI_MONITER_REQUEST)) {
			log.info("监控车辆位置==========");
		}else if (sendType.equals(GPSCommand.GPS_TAXI_CANCEL_REQUEST)) {
			log.info("取消打车请求==========");
		}
		try {
			login();
			writer.write(message);
			writer.flush();
			char chars[] = new char[64];
			int len;
			// socket.setSoTimeout(5 * 1000);
			StringBuffer sb = new StringBuffer();
			while ((len = reader.read(chars)) != -1) {
				sb.append(new String(chars, 0, len));
				if (sb.indexOf(">>") != -1) {
					break;
				}
			}
			return sb != null ? sb.toString() : null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			this.clientSync.cancel();
		}
	}

	public void setWriter(Writer writer) {
		this.writer = writer;
	}

	public void setReader(Reader reader) {
		this.reader = reader;
	}

	public Writer getWriter() {
		return writer;
	}

	public Reader getReader() {
		return reader;
	}

	public GpsClientAsync getClientAsync() {
		return clientAsync;
	}

	public void setClientAsync(GpsClientAsync clientAsync) {
		this.clientAsync = clientAsync;
	}

}
