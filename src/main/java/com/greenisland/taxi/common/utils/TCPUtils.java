package com.greenisland.taxi.common.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import org.springframework.util.StringUtils;

import com.greenisland.taxi.domain.CallApplyInfo;
import com.greenisland.taxi.domain.LocationInfo;
import com.greenisland.taxi.domain.UserInfo;

/**
 * 
 * @author Jerry
 * @E-mail jerry.ma@bstek.com
 * @version 2013-9-9下午1:53:42
 */
public class TCPUtils {
	/**
	 * 得到登陆信息
	 * 
	 * @param userName
	 *            TODO
	 * @param password
	 *            TODO
	 * 
	 * @return
	 */
	public static String getLoginMsg(String userName, String password) {
		StringBuilder logMsg = new StringBuilder("<<0001,0000000001,");
		if (org.springframework.util.StringUtils.hasText(userName)) {
			logMsg.append(userName);
		}
		logMsg.append(",");
		if (StringUtils.hasText(password)) {
			logMsg.append(password);
		}
		logMsg.append(">>");
		return logMsg.toString();
	}

	/**
	 * 周边出租车查询请求参数
	 * 
	 * @param gpsLongitude
	 * @param gpsLatitude
	 * @param radius
	 * @return
	 * @throws Exception
	 */
	public static String getTaxis(String gpsLongitude, String gpsLatitude, String radius) throws Exception {
		StringBuilder qeuryTaxisMsg = new StringBuilder("<<0002,");
		qeuryTaxisMsg.append(generateProcessId());
		qeuryTaxisMsg.append("," + gpsLongitude + "," + gpsLatitude + "," + radius);
		qeuryTaxisMsg.append(">>");
		return qeuryTaxisMsg.toString();

	}

	/**
	 * 监控出租车位置信息
	 * 
	 * @param applyId
	 * @param plateNumber
	 * @return
	 */
	public static String getMonitorMessage(String applyId, String plateNumber) {
		StringBuilder monitorMessage = new StringBuilder("<<0005,");
		monitorMessage.append(generateProcessId());
		monitorMessage.append("," + applyId + "," + plateNumber);
		monitorMessage.append(">>");
		return monitorMessage.toString();
	}

	/**
	 * 获取即时叫车请求信息
	 * 
	 * @param applyInfo
	 * @param applyId
	 * @param location
	 * @param user
	 * @return
	 */
	public static String getCallApply(CallApplyInfo applyInfo, String applyId, LocationInfo location, UserInfo user) {
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		StringBuilder callApplyMsg = new StringBuilder("<<0003,");
		String sLocation = applyInfo.getStartLocation();
		String eLocation = applyInfo.getEndLocation();
		sLocation = sLocation.replaceAll(",", "");
		eLocation = eLocation.replaceAll(",", "");
		callApplyMsg.append(generateProcessId());
		Date callTime = applyInfo.getCallTime();
		callApplyMsg.append("," + applyId + "," + applyInfo.getCallType() + "," + sLocation + "," + eLocation);
		callApplyMsg.append("," + location.getGpsLongitude() + "," + location.getGpsLatitude() + "," + format.format(callTime));
		callApplyMsg.append("," + applyInfo.getCallScope() + "," + user.getUserName() + "," + user.getPhoneNumber());
		callApplyMsg.append("," + user.getAddress() + ">>");
		return callApplyMsg.toString();
	}

	/**
	 * 产生流水号
	 * 
	 * @return
	 */
	private static String generateProcessId() {
		Random random = new Random();
		String processId = "";
		for (int i = 0; i < 10; i++) {
			String rand = String.valueOf(random.nextInt(10));
			processId += rand;
		}
		return processId;
	}

}
