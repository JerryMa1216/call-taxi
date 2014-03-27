package com.greenisland.taxi.gateway.gps.multithreading;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.greenisland.taxi.common.constant.GPSCommand;
import com.greenisland.taxi.gateway.gps.SyncResponse;

public class GpsSender {
	private static Logger log = Logger.getLogger(GpsSender.class);
	private byte[] sendData;
	private byte[] getData;
	private String responseData;
	private DataOutputStream out;
	private DataInputStream in;
	private SyncResponse syncResponse;

	public GpsSender(byte[] sendData, DataOutputStream out, DataInputStream in, SyncResponse syncResponse) {
		super();
		this.sendData = sendData;
		this.out = out;
		this.in = in;
		this.syncResponse = syncResponse;
	}

	public boolean start() throws Exception {
		boolean success = false;
		if (out != null && null != sendData) {
			sendMsg(sendData);
			byte[] returnData = getInData();
			setGetData(returnData);
			if (returnData != null) {
				String resultValue = new String(getGetData(), 0, in.readInt(), "GBK");
				String msg1 = resultValue.substring(2);
				String msg2 = msg1.substring(0, msg1.indexOf(">"));
				// 消息id
				String msgId = msg2.substring(0, 4);
				log.info("======响应消息类型=======");
				log.info(msgId);
				log.info("======响应消息类型=======");
				if (msgId.equals(Integer.toString(GPSCommand.GPS_TAXI_RESP))) {
					syncResponse.handlerResponse(resultValue);
				} else if (msgId.equals(Integer.toString(GPSCommand.GPS_HEARTBEAT))) {
					log.info("====== 心跳包链路响应 =====");
					log.info(resultValue);
					log.info("====== 心跳包链路响应 =====");
				} else if (msgId.equals(Integer.toString(GPSCommand.GPS_CALL_RESP))) {
					log.info("======= 召车响应 ======");
					log.info(resultValue);
					log.info("======= 召车响应 ======");
				} else if (msgId.equals(Integer.toString(GPSCommand.GPS_LOGIN))) {
					log.info("=======登录GPS服务器响应=======");
					log.info(resultValue);
					log.info("=======登录GPS服务器响应=======");
				} else if (msgId.equals(Integer.toString(GPSCommand.GPS_AROUND_TAXIS))) {
					log.info("=======周边车辆查询=======");
					log.info(msgId);
					log.info("=======周边车辆查询=======");
					setResponseData(resultValue);
				} else if (msgId.equals(Integer.toString(GPSCommand.GPS_TAXI_MONITER))) {
					log.info("=======出租车监控响应=======");
					log.info(msgId);
					log.info("=======出租车监控响应=======");
					setResponseData(resultValue);
				}
				success = true;
			} else {
				success = false;
			}
		}
		return success;
	}

	/**
	 * 发送信息
	 * 
	 * @param data
	 * @return
	 */
	private boolean sendMsg(byte[] data) {
		try {
			out.write(data);
			out.flush();
			return true;
		} catch (Exception e) {
			log.error("发送数据失败，无字节输入！");
		}
		return false;
	}

	/**
	 * 读取服务器响应信息
	 * 
	 * @return
	 * @throws IOException
	 */
	private byte[] getInData() throws IOException {
		try {
			int len = in.readInt();
			if (null != in && 0 != len) {
				byte[] data = new byte[len];
				in.read(data);
				return data;
			} else {
				return null;
			}
		} catch (NullPointerException ef) {
			log.error("[GPS]本连接上接收字节信息：无流输入！");
			return null;
		} catch (EOFException eof) {
			log.error("[GPS]本连接上接收字节信息： " + eof.getMessage());
			return null;
		}
	}

	public byte[] getGetData() {
		return getData;
	}

	public void setGetData(byte[] getData) {
		this.getData = getData;
	}

	public String getResponseData() {
		return responseData;
	}

	public void setResponseData(String responseData) {
		this.responseData = responseData;
	}

}
