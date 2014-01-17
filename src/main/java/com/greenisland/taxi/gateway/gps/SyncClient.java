package com.greenisland.taxi.gateway.gps;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

/**
 * 
 * @author Jerry
 * @E-mail jerry.ma@bstek.com
 * @version 2013-10-26下午2:09:57
 */
@Component("syncClient")
public class SyncClient {
	private static Logger log = Logger.getLogger(SyncClient.class.getName());
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
		log.info("=============获取GPS返回值为： ["+returnData+"]");
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
		System.out.println("设置完毕！");
		log.info("=============GPS返回值["+data+"]设置成功========");
	}

	public synchronized boolean sendMessage(String message) {
		boolean flag = tcpClient.sendMessage(tcpClient.getSocket(), message);
		return flag;
	}
}
