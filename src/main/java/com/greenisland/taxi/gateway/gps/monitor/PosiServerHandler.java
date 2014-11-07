package com.greenisland.taxi.gateway.gps.monitor;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

import com.greenisland.taxi.gateway.gps.SyncResponse;

public class PosiServerHandler implements Runnable {
	private static Logger log = Logger.getLogger(PosiServerHandler.class.getName());
	private SyncResponse syncResponse;
	private DatagramSocket ms;
	private boolean isRun = false;

	public PosiServerHandler(SyncResponse syncResponse, DatagramSocket ms, boolean isRun) {
		super();
		this.syncResponse = syncResponse;
		this.ms = ms;
		this.isRun = isRun;
	}

	@Override
	public void run() {
		byte[] buf = new byte[4096];
		DatagramPacket recvPack = new DatagramPacket(buf, buf.length);
		String strRead = "";
		while (isRun) {
			try {
				handle(buf, recvPack, strRead);
			} catch (Exception e) {
				log.info("Position service udp receive exception:" + e.getMessage());
				if (!isRun)
					return;
			}
		}
	}

	private void handle(byte[] buf, DatagramPacket recvPack, String strRead) {
		try {
			recvPack.setData(buf);
			recvPack.setLength(4096);
			ms.receive(recvPack);
			strRead = new String(recvPack.getData(), 0, recvPack.getLength(), "GBK");
			log.info("=====抢答响应数据：" + strRead);
			if (StringUtils.hasText(strRead) && strRead.indexOf(">>") != -1) {
				log.info("抢答处理数据");
				this.syncResponse.handlerResponse(strRead, null);
			}
		} catch (Exception e) {

		}
	}

}
