package com.greenisland.taxi.common.constant;

/**
 * GPS平台响应代码
 * 
 * @author Jerry
 * @E-mail jerry.ma@bstek.com
 * @version 2013-9-16下午6:13:23
 */
public interface GPSCommand {
	/**
	 * 返回登录是否成功
	 */
	int GPS_LOGIN = 1001;
	/**
	 * 周边车辆查询应答
	 */
	int GPS_AROUND_TAXIS = 1002;
	/**
	 * 乘客召车响应
	 */
	int GPS_CALL_RESP = 1003;
	/**
	 * 召车抢答上报
	 */
	int GPS_TAXI_RESP = 1004;
	/**
	 * 车辆位置跟踪应答
	 */
	int GPS_TAXI_MONITER = 1005;
	/**
	 * 心跳包应答
	 */
	int GPS_HEARTBEAT = 1099;
}
