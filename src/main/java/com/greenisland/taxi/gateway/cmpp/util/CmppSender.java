package com.greenisland.taxi.gateway.cmpp.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.greenisland.taxi.gateway.cmpp.domain.MsgActiveTestResp;
import com.greenisland.taxi.gateway.cmpp.domain.MsgCommand;
import com.greenisland.taxi.gateway.cmpp.domain.MsgConnectResp;
import com.greenisland.taxi.gateway.cmpp.domain.MsgHead;
import com.greenisland.taxi.gateway.cmpp.domain.MsgSubmitResp;

/**
 * 
 * @author Jerry
 * @E-mail jerry.ma@bstek.com
 * @version 2013-8-26下午2:04:51
 */
public class CmppSender {
	private static Log log = LogFactory.getLog(CmppSender.class);
	private List<byte[]> sendData = new ArrayList<byte[]>();// 需要发出的二进制数据队列
	private List<byte[]> getData = new ArrayList<byte[]>();// 需要接受的二进制队列
	private DataOutputStream out;
	private DataInputStream in;

	public CmppSender(List<byte[]> sendData, DataOutputStream out, DataInputStream in) {
		super();
		this.sendData = sendData;
		this.out = out;
		this.in = in;
	}

	public boolean start() throws Exception {
		boolean success = false;
		if (out != null && null != sendData) {
			for (byte[] data : sendData) {
				sendMsg(data);
				byte[] returnData = getInData();
				if (null != returnData) {
					getData.add(returnData);
				}
			}
		}
		if (in != null && null != getData) {
			for (byte[] data : getData) {
				MsgHead head = new MsgHead(data);
				switch (head.getCommandId()) {
				case MsgCommand.CMPP_CONNECT_RESP:
					MsgConnectResp connectResp = new MsgConnectResp(data);
					if (connectResp.getStatus() == 0) {
						success = true;
					}
					log.info("CMPP_CONNECT_RESP,status :" + connectResp.getStatus() + " 序列号： " + connectResp.getSequenceId());
					break;
				case MsgCommand.CMPP_ACTIVE_TEST_RESP:
					MsgActiveTestResp activeResp = new MsgActiveTestResp(data);
					log.info("短信网关与短信网关进行连接检查" + " 序列号： " + activeResp.getSequenceId());
					break;
				case MsgCommand.CMPP_SUBMIT_RESP:
					MsgSubmitResp submitResp = new MsgSubmitResp(data);
					if (submitResp.getResult() == 0) {
						success = true;
					}
					log.info("CMPP_SUBMIT_RESP,status : " + submitResp.getResult() + " 序列号： " + submitResp.getSequenceId());
					break;
				case MsgCommand.CMPP_TERMINATE_RESP:
					log.info("拆除与ISMG的链接" + " 序列号： " + head.getSequenceId());
					break;
				case MsgCommand.CMPP_CANCEL_RESP:
					log.info("CMPP_CANCEL_RESP 序列号： " + head.getSequenceId());
					break;
				case MsgCommand.CMPP_CANCEL:
					log.info("CMPP_CANCEL 序列号：" + head.getSequenceId());
					break;
				case MsgCommand.CMPP_QUERY:
					log.info("CMPP_QUERY 序列号：" + head.getSequenceId());
					break;
				case MsgCommand.CMPP_QUERY_RESP:
					log.info("CMPP_QUERY_RESP 序列号：" + head.getSequenceId());
					break;
				case MsgCommand.CMPP_TERMINATE:
					log.info("CMPP_TERMINATE 序列号：" + head.getSequenceId());
					break;
				case MsgCommand.CMPP_CONNECT:
					log.info("CMPP_CONNECT 序列号：" + head.getSequenceId());
					break;
				case MsgCommand.CMPP_ACTIVE_TEST:
					log.info("CMPP_ACTIVE_TEST 序列号：" + head.getSequenceId());
					break;
				case MsgCommand.CMPP_SUBMIT:
					log.info("CMPP_SUBMIT 序列号：" + head.getSequenceId());
					break;
				default:
					log.error("无法解析IMSP返回的包结构：包长度为" + head.getTotalLength());
					break;
				}
			}
		}
		return success;
	}

	public List<byte[]> getGetData() {
		return getData;
	}

	/**
	 * 在本连接中发送已打包后的消息的字节
	 * 
	 * @param data
	 * @return
	 * @throws Exception
	 */
	private boolean sendMsg(byte[] data) {
		try {
			out.write(data);
			out.flush();
			return true;
		} catch (Exception e) {
			log.error("在本连结上发送已打包后的消息的字节:无字节输入");
		}
		return false;
	}

	private byte[] getInData() throws IOException{
		try {
			int len = in.readInt();
			if (null != in && 0 != len) {
				byte[] data = new byte[len - 4];
				in.read(data);
				return data;
			} else {
				return null;
			}
		} catch (NullPointerException ef) {
			log.error("在本连结上接受字节消息:无流输入");
			return null;
		} catch (EOFException eof) {
			log.error("在本连结上接受字节消息:" + eof.getMessage());
			return null;
		} 
	}
}
