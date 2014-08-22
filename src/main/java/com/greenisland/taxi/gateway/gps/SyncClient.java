package com.greenisland.taxi.gateway.gps;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.greenisland.taxi.common.constant.GPSCommand;
import com.greenisland.taxi.common.utils.TCPUtils;

/**
 * 
 * @author Jerry
 * @E-mail jerry.ma@bstek.com
 * @version 2013-10-26下午2:09:57
 */
@Component("syncClient")
public class SyncClient {
	private static Logger log = Logger.getLogger(SyncClient.class.getName());
	@Resource(name = "syncResponse")
	private SyncResponse synResponse;
	private String result;// 线程同步锁变量
	private GpsClient gpsClient;

	public GpsClient getGpsClient() {
		return gpsClient;
	}

	public void setGpsClient(GpsClient gpsClient) {
		this.gpsClient = gpsClient;
	}

	/**
	 * 同步get方法
	 * 
	 * @return
	 */
	public synchronized String getResult() {
		while (result == null) {
			try {
				wait(10000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		String returnData = result;
		result = null;
		notify();
		return returnData;
	}

	/**
	 * 同步set方法
	 * 
	 * @param data
	 */
	public synchronized void setResult(String data) {
		while (result != null) {
			try {
				wait(10000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		this.result = data;
		notify();
	}

	// public String getResult() {
	// return result;
	// }
	//
	// public void setResult(String result) {
	// this.result = result;
	// }

	/**
	 * 发送信息
	 * 
	 * @param message
	 * @return
	 * @throws Exception
	 */
	public String sendMessage(String message) throws Exception {
		String msg1 = message.substring(2);
		// 请求消息id
		String msgId = msg1.substring(0, 4);
		log.info("========== 向GPS发送数据： " + message);
		GpsClient gpsClient = new GpsClient(this, synResponse);
		setGpsClient(gpsClient);
		String returnData = null;
		String loginMessage = TCPUtils.getLoginMsg(gpsClient.getUsername(), gpsClient.getPassword());
		log.info("======== 登陆GPS服务器 ========");
		boolean flag = gpsClient.sendMessage(gpsClient.getSocket(), loginMessage);
		// if (flag) {
		// returnData = getResult();
		// int count = 0;
		// while (returnData.indexOf("ER") != -1) {
		// log.info("========= 登陆失败，重新登陆 =========");
		// count++;
		// flag = gpsClient.sendMessage(gpsClient.getSocket(), loginMessage);
		// if (flag) {
		// returnData = getResult();
		// }
		// if (count >=
		// (Integer.parseInt(Configure.getString("gpsConnectCount")) - 1)) {
		// return null;
		// }
		// }
		// } else {
		// return null;
		// }
		log.info("========= 登陆成功 ==========");
		flag = gpsClient.sendMessage(gpsClient.getSocket(), message);
		if (flag) {
			// 请求为周边车辆查询
			if (!msgId.equals(GPSCommand.GPS_CALL_REQUEST)) {
				// Thread.sleep(170);
				returnData = getResult();
				// 执行完成，关闭socket连接
				gpsClient.cancel();
			}
		}
		return returnData;
	}

}
