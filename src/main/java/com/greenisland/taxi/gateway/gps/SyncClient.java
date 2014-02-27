package com.greenisland.taxi.gateway.gps;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

/**
 * 
 * @author Jerry
 * @E-mail jerry.ma@bstek.com
 * @version 2013-10-26下午2:09:57
 */
@Component("syncClient")
public class SyncClient {
	@Resource
	private TCPClient tcpClient;
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
		//socket为空时，不发送任何数据
		if(tcpClient.getSocket() != null){
			flag = tcpClient.sendMessage(tcpClient.getSocket(), message);
		}
		return flag;
	}
}
