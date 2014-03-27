package com.greenisland.taxi.gateway.gps;

import org.apache.log4j.Logger;

public class ActivityConnectThread extends Thread {
	private static Logger log = Logger.getLogger(ActivityConnectThread.class.getName());
	private SyncClient client;
	public boolean isRunning = false;

	public ActivityConnectThread(SyncClient client) {
		this.client = client;
		this.start();
	}

	@Override
	public synchronized void start() {
		isRunning = true;
		super.start();
	}

	@Override
	public void run() {
		super.run();
		while (isRunning) {
			try {
				sleep(7000);
				isRunning = client.sendMessage("<<0099,0000000009,0>>");
			} catch (Exception e) {
				log.info(e.getMessage());
			}
		}
	}

}
