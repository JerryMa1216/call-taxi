package com.greenisland.taxi.gateway.job;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.bstek.dorado.core.Configure;
import com.greenisland.taxi.gateway.gps.multithreading.GpsContainer;

public class ActivityGPSConnect extends QuartzJobBean{
	private static Logger log = Logger.getLogger(ActivityGPSConnect.class.getName());
	private GpsContainer gpsContainer;
	
	public void setGpsContainer(GpsContainer gpsContainer) {
		this.gpsContainer = gpsContainer;
	}

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		log.info("==========开始链路检查==========");
		int count = 0;
		boolean result = gpsContainer.activityTestGPS();
		while (!result) {
			count++;
			result = gpsContainer.activityTestGPS();
			if (count >= (Integer.parseInt(Configure.getString("gpsConnectCount")) - 1)) {
				break;
			}
		}
		log.info("==========链路检查结束==========");
	}
}
