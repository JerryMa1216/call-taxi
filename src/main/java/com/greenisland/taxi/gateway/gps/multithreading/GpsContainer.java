package com.greenisland.taxi.gateway.gps.multithreading;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.bstek.dorado.core.Configure;
import com.greenisland.taxi.common.utils.TCPUtils;
import com.greenisland.taxi.gateway.gps.SyncResponse;

@Component("gpsContainer")
public class GpsContainer {
	private static Logger log = Logger.getLogger(GpsContainer.class);
	private Socket msgSocket;
	private DataInputStream in;
	private DataOutputStream out;
	@Resource(name = "syncResponse")
	private SyncResponse syncResponse;

	public Socket getSocketInstance() {
		if (null == msgSocket || msgSocket.isClosed() || !msgSocket.isConnected()) {
			try {
				in = null;
				out = null;
				msgSocket = new Socket(Configure.getString("host"), Integer.parseInt(Configure.getString("port")));
				msgSocket.setKeepAlive(true);
				in = getSocketDIS();
				out = getSocketDOS();
				int count = 0;
				boolean result = connectGPS();
				while (!result) {
					count++;
					result = connectGPS();
					if (count >= (Integer.parseInt(Configure.getString("gpsConnectCount")) - 1)) {
						break;
					}
				}
			} catch (UnknownHostException e) {
				log.error("==========Socket连接GPS网关端口号不正确： " + e.getMessage());
			} catch (IOException e) {
				log.error("==========Socket连接短信网关失败： " + e.getMessage());
			}
		}
		return msgSocket;
	}

	public DataInputStream getSocketDIS() {
		if (in == null || null == msgSocket || msgSocket.isClosed() || !msgSocket.isConnected()) {
			try {
				in = new DataInputStream(getSocketInstance().getInputStream());
			} catch (IOException e) {
				in = null;
			}
		}
		return in;
	}

	public DataOutputStream getSocketDOS() {
		if (out == null || null == msgSocket || msgSocket.isClosed() || !msgSocket.isConnected()) {
			try {
				out = new DataOutputStream(getSocketInstance().getOutputStream());
			} catch (IOException e) {
				out = null;
			}
		}
		return out;
	}

	/**
	 * 初始化socket连接
	 * 
	 * @return
	 */
	private boolean connectGPS() {
		try {
			log.info("==========请求连接到GPS服务器...==========");
			String loginMsg = TCPUtils.getLoginMsg(Configure.getString("username"), Configure.getString("password"));
			GpsSender sender = new GpsSender(loginMsg.getBytes("GBK"), getSocketDOS(), getSocketDIS(), syncResponse);
			boolean success = sender.start();
			if (success) {
				log.info("==========请求连接到GPS服务器，连接成功！");
			} else {
				log.info("==========请求连接到GPS服务器，连接失败！");
			}
			return success;
		} catch (Exception e) {
			log.error(e.getMessage());
			try {
				out.close();
			} catch (IOException e2) {
				out = null;
			}
			return false;
		}
	}

	/**
	 * 发送信息
	 * 
	 * @param msg
	 * @return
	 */
	public String sendMsg(String msg) {
		try {
			if (StringUtils.hasText(msg)) {
				log.info("==========向GPS服务器发送数据： " + msg);
				GpsSender sender = new GpsSender(msg.getBytes("GBK"), getSocketDOS(), getSocketDIS(), syncResponse);
				boolean success = sender.start();
				if (success) {
					return sender.getResponseData();
				}
			}
			return null;
		} catch (Exception e) {
			try {
				out.close();
			} catch (IOException e1) {
				out = null;
			}
			return null;
		}
	}

	/**
	 * GPS服务器链路检查
	 * 
	 * @return
	 */
	public boolean activityTestGPS() {
		try {
			String testMsg = "<<0099,0000000009,0>>";
			GpsSender sender = new GpsSender(testMsg.getBytes("GBK"), out, in, syncResponse);
			sender.start();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			try {
				out.close();
			} catch (Exception e2) {
				out = null;
			}
			log.error("==========GPS连接检查失败： " + e.getMessage());
			return false;
		}
	}
}
