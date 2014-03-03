package com.greenisland.taxi.gateway.gps;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import com.greenisland.taxi.common.utils.TCPUtils;

@Component
public class CheckConnectionThread extends Thread implements InitializingBean {
	private static Logger log = Logger.getLogger(CheckConnectionThread.class.getName());
	@Resource(name = "syncResponse")
	private SyncResponse synResponse;
	@Resource(name = "syncClient")
	private SyncClient syncClient;
	private TCPClient tcpClient;

	@Override
	public synchronized void start() {
		log.info("==========启动监听连接线程==========");
		tcpClient = new TCPClient(syncClient, synResponse);
		super.start();
	}

	@Override
	public void run() {
		super.run();
		while (true) {
			boolean flag = tcpClient.isServerClose(tcpClient.getSocket());
			if (flag) {
				try {
					log.info("==========连接已断开,关闭线程==========");
					tcpClient.cancel();
					tcpClient.join();
					log.info("==========重新初始化[socket]连接，开始==========");
					log.info("==========重连等待70秒,开始==========");
					sleep(7000);
					log.info("==========重连等待70秒，结束==========");
					// 重新初始化socket连接
					tcpClient = new TCPClient(syncClient, synResponse);
					syncClient.setTcpClient(tcpClient);
					log.info("==========重新初始化socket连接，成功==========");
					// 登陆socket
					tcpClient.sendMessage(tcpClient.getSocket(), TCPUtils.getLoginMsg(tcpClient.getUsername(), tcpClient.getPassword()));
					String returnData = syncClient.getResult();
					log.info("======登陆成功,返回信息：[" + returnData + "]========");
				} catch (Exception e) {
					log.error(e.getMessage());
				}
			}
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		start();
	}

}
