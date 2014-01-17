package com.greenisland.taxi.gateway.cmpp.util;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.bstek.dorado.core.Configure;

/**
 * 接口调用
 * 
 * @author Jerry
 * @E-mail jerry.ma@bstek.com
 * @version 2013-8-26下午2:29:43
 */
public class MsgActivityTimer extends QuartzJobBean {
	private MsgContainer msgContainer;

	public void setMsgContainer(MsgContainer msgContainer) {
		this.msgContainer = msgContainer;
	}

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		System.out.println("×××××××××××××开始链路检查××××××××××××××");
		int count = 0;
		boolean result = msgContainer.activityTestISMG();
		while (!result) {
			count++;
			result = msgContainer.activityTestISMG();
			if (count >= (Integer.parseInt(Configure.getString("connectCount")) - 1)) {
				break;
			}
		}
		System.out.println("×××××××××××××链路检查结束××××××××××××××");
	}
}
