package com.greenisland.taxi.gateway.gps;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import com.greenisland.taxi.common.utils.TCPUtils;

/**
 * 
 * @author Jerry
 * @E-mail jerry.ma@bstek.com
 * @version 2013-10-26下午2:09:57
 */
@Component("syncClient")
public class SyncClient implements InitializingBean {
	private static Logger log = Logger.getLogger(SyncClient.class.getName());
	private TCPClient tcpClient;
	@Resource(name = "syncResponse")
	private SyncResponse synResponse;
	private String result;

	public synchronized String getResult() {
		while (result == null) {
			try {
				wait();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		String returnData = result;
		result = null;
		notify();
		return returnData;
	}

	public synchronized void setResult(String data) {
		while (result != null) {
			try {
				wait();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		this.result = data;
		notify();
	}

	public synchronized boolean sendMessage(String message) {
		boolean flag = false;
		// socket为空时，不发送任何数据
		if (tcpClient.getSocket() != null) {
			flag = tcpClient.sendMessage(tcpClient.getSocket(), message);
		}
		return flag;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		tcpClient = new TCPClient(this, synResponse);
		tcpClient.sendMessage(tcpClient.getSocket(), TCPUtils.getLoginMsg(tcpClient.getUsername(), tcpClient.getPassword()));
		String returnData = getResult();
		log.info("======登陆成功,返回信息：[" + returnData + "]========");
	}

	public TCPClient getTcpClient() {
		return tcpClient;
	}

	public void setTcpClient(TCPClient tcpClient) {
		this.tcpClient = tcpClient;
	}
}
