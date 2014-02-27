package com.greenisland.taxi.gateway.gps;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import com.bstek.dorado.core.Configure;
import com.greenisland.taxi.common.constant.GPSCommand;
import com.greenisland.taxi.common.utils.TCPUtils;

/**
 * gps系统连接类
 * 
 * @author Jerry
 * @E-mail jerry.ma@bstek.com
 * @version 2013-10-22上午1:32:36
 */
@Component("tcpClient")
public class TCPClient extends Thread implements InitializingBean {
	private static Logger log = Logger.getLogger(TCPClient.class.getName());
	@Resource
	private SyncClient client;
	@Resource
	private SyncResponse synResponse;
	public String host;
	public Integer port;
	private Socket socket = null;
	private boolean isRunning = false;
	private String resultValue;
	private String username;
	private String password;

	/**
	 * 初始化客户端socket连接
	 */
	public void initServer() {
		try {
			log.info("===============");
			log.info("初始化客户端......");
			log.info("===============");
			setHost(Configure.getString("host"));
			setPort(Integer.parseInt(Configure.getString("port")));
			setUsername(Configure.getString("username"));
			setPassword(Configure.getString("password"));
			setSocket(new Socket(getHost(), getPort()));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Boolean sendMessage(Socket socket, String datas) {
		try {
			if (datas != null) {
				socket.getOutputStream().write(datas.getBytes("GBK"));
			}
			return true;
		} catch (Exception e) {
			log.info("======发送数据失败： " + getHost() + ": " + getPort() + " =====");
			log.error(e.getMessage());
			log.info("===============");
//			log.info("======重新初始化socket通道======");
//			initServer();
//			isRunning = !sendMessage(getSocket(), TCPUtils.getLoginMsg(username, password));
//			log.info("======成功初始化socket通道======");
			return false;
		}
	}

	@Override
	public void run() {
		super.run();
		int rLen = 0;
		byte[] data = new byte[10 * 1024];
		while (true) {
			try {
				isRunning = isServerClose(getSocket());
				if (!isRunning) {
					rLen = getSocket().getInputStream().read(data);
					if (rLen > 0) {
						resultValue = new String(data, 0, rLen, "GBK");
						String msg1 = resultValue.substring(2);
						String msg2 = msg1.substring(0, msg1.indexOf(">"));
						// 消息id
						String msgId = msg2.substring(0, 4);
						log.info("======响应消息类型=======");
						log.info(msgId);
						log.info("======响应消息类型=======");
						if (msgId.equals(Integer.toString(GPSCommand.GPS_TAXI_RESP))) {
							synResponse.handlerResponse(resultValue);
						} else if (msgId.equals(Integer.toString(GPSCommand.GPS_HEARTBEAT))) {
							log.info("====== 心跳包链路响应 =====");
							log.info(resultValue);
							log.info("====== 心跳包链路响应 =====");
						} else if (msgId.equals(Integer.toString(GPSCommand.GPS_CALL_RESP))) {
							log.info("======= 召车响应 ======");
							log.info(resultValue);
							log.info("======= 召车响应 ======");
						} else {
							synchronized (client) {
								client.setResult(resultValue);
							}
						}
					}
				}
				// 断开连接
				while (isRunning) {
					try {
						log.error("连接已断开");
						//将原socket值为空，直到连接成功后再发送数据。
						setSocket(null);
						log.info("======重新连接 ======");
						initServer();
						isRunning = !sendMessage(getSocket(), TCPUtils.getLoginMsg(username, password));
						log.info("======重新连接成功=====");
					} catch (Exception e) {
						e.printStackTrace();
						log.info("=====建立连接失败====");
						isRunning = true;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				log.error("socket exception.");
				return;
			}
		}
	}

	/**
	 * 判断socket是否断开
	 * 
	 * @param socket
	 * @return true:断开 false：未断开
	 */
	public Boolean isServerClose(Socket socket) {
		try {
			socket.sendUrgentData(0);// 发送1个字节的紧急数据，默认情况下，服务器端没有开启紧急数据处理，不影响正常通信
			return false;
		} catch (Exception se) {
			return true;
		}
	}

	public void afterPropertiesSet() throws Exception {
		this.initServer();
		this.start();
		sendMessage(getSocket(), TCPUtils.getLoginMsg(username, password));
		String returnData = client.getResult();
		log.info("======登陆成功,返回信息：[" + returnData + "]========");
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
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

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

}
