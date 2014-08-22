package com.greenisland.taxi.controller;

import java.util.Date;

import org.apache.log4j.Logger;

import com.greenisland.taxi.common.constant.ApplicationState;
import com.greenisland.taxi.common.constant.ResponseState;
import com.greenisland.taxi.domain.CallApplyInfo;
import com.greenisland.taxi.gateway.gps.GpsService;
import com.greenisland.taxi.manager.CallApplyInfoService;

public class CallApplyThread extends Thread {
	private static Logger log = Logger.getLogger(CallApplyThread.class.getName());
	private CallApplyInfoService applyInfoService;

	private String applyId;
	// private SyncClient syncClient;
	private GpsService gpsService;

	public CallApplyThread() {
	}

	// public CallApplyThread(CallApplyInfoService applyInfoService, String
	// applyId, SyncClient syncClient) {
	// super();
	// this.applyInfoService = applyInfoService;
	// this.applyId = applyId;
	// this.syncClient = syncClient;
	// new Thread(this).start();
	// }

	public CallApplyThread(CallApplyInfoService applyInfoService, String applyId, GpsService gpsService) {
		super();
		this.applyInfoService = applyInfoService;
		this.applyId = applyId;
		this.gpsService = gpsService;
		new Thread(this).start();
	}

	@Override
	public void run() {
		super.run();
		try {
			long firstTime = new Date().getTime();
			log.info("======从[" + firstTime + "]开始倒计时任务======");
			while (true) {
				long newTime = new Date().getTime();
				if (newTime - firstTime == 50000) {
					CallApplyInfo applyInfo = this.applyInfoService.getCallApplyInfoById(applyId);
					if (applyInfo != null) {
						String responseState = applyInfo.getResponseState();
						if (responseState.equals(ResponseState.WAIT_RESPONSE)) {
							log.info("==========叫车无响应，开始关闭socket及监听线程==========");
							// this.syncClient.getGpsClient().cancel();
							gpsService.getClientAsync().cancel();
							log.info("==========关闭socket及监听线程成功==========");
							log.info("==========未响应请求,更新记录为无效==========");
							applyInfo.setState(ApplicationState.INVALIDATION);
							applyInfo.setResponseState(ResponseState.NO_RESPONSE);
							applyInfo.setDeleteFlag("Y");
							this.applyInfoService.updateApplyInfo(applyInfo);
						}
					}
					break;
				}
			}
			log.info("==========倒计时结束==========");
		} catch (Exception e) {
			log.info("=====倒计时任务处理异常=====" + e.getMessage());
		}
	}
}
