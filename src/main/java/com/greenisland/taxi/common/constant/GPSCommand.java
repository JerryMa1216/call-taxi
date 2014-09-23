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
	 * 取消打车应答
	 */
	int GPS_TAXI_CANCEL = 1007;
	/**
	 * 心跳包应答
	 */
	int GPS_HEARTBEAT = 1099;

	/**
	 * 登录请求
	 */
	String GPS_LOGIN_REQUEST = "0001";
	/**
	 * 周边车辆查询
	 */
	String GPS_AROUND_TAXIS_REQUEST = "0002";
	/**
	 * 乘客召车
	 */
	String GPS_CALL_REQUEST = "0003";
	/**
	 * 车辆位置跟踪
	 */
	String GPS_TAXI_MONITER_REQUEST = "0005";

	/**
	 * 取消打车请求
	 */
	String GPS_TAXI_CANCEL_REQUEST = "0007";
}
