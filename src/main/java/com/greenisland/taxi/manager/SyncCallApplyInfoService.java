package com.greenisland.taxi.manager;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.greenisland.taxi.common.constant.ApplicationState;
import com.greenisland.taxi.common.constant.ResponseState;
import com.greenisland.taxi.domain.CallApplyInfo;

/**
 * 打车请求倒计时类
 * 
 * @author jerry
 * 
 */
@Component
public class SyncCallApplyInfoService {
	private static Logger log = Logger.getLogger(SyncCallApplyInfoService.class.getName());
	@Resource
	private CallApplyInfoService applyInfoService;

	public synchronized boolean updateApplyState(String applyId) {
		try {
			log.info("=======开始倒计时任务======");
			Thread.sleep(60000);
			CallApplyInfo applyInfo = this.applyInfoService.getCallApplyInfoById(applyId);
			String responseState = applyInfo.getResponseState();
			if (responseState.equals(ResponseState.NO_RESPONSE)) {
				applyInfo.setState(ApplicationState.INVALIDATION);
				applyInfo.setDeleteFlag("Y");
				this.applyInfoService.updateApplyInfo(applyInfo);
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			log.error("置打车请求无效失败： " + e.getMessage());
			return false;
		}
	}
}
