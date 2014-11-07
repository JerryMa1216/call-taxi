package com.greenisland.taxi.gateway.gps.monitor;

import java.net.DatagramSocket;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import com.greenisland.taxi.gateway.gps.SyncResponse;

@Component
public class PosiSvr implements InitializingBean {
	private static Logger log = Logger.getLogger(PosiSvr.class.getName());
	private DatagramSocket ms = null;
	private int nSerPort = 8877;
	private boolean bRun = false;

	@Resource(name = "syncResponse")
	private SyncResponse syncResponse;

	public void initServer() {
		try {
			this.bRun = true;
			this.ms = new DatagramSocket(this.nSerPort);
			log.info("叫车响应数据接收模块,侦听端口:" + nSerPort);
			new Thread(new PosiServerHandler(syncResponse, ms, bRun)).start();
		} catch (Exception e) {
			log.info("Position service udp start fail!");
		}
	}

	public void stopServer() {
		bRun = false;
		try {
			ms.close();
		} catch (Exception ioe) {
			log.warn(ioe.getMessage());
		}
		ms = null;
		log.info("Position service udp module stop!");
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		System.out.println("UDP init");
		initServer();
	}

}
