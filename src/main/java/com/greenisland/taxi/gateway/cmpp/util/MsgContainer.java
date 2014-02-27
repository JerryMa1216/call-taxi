package com.greenisland.taxi.gateway.cmpp.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import com.bstek.dorado.core.Configure;
import com.greenisland.taxi.gateway.cmpp.domain.MsgCommand;
import com.greenisland.taxi.gateway.cmpp.domain.MsgConnect;
import com.greenisland.taxi.gateway.cmpp.domain.MsgHead;
import com.greenisland.taxi.gateway.cmpp.domain.MsgSubmit;

/**
 * 短信接口容器，单例获取链接对象
 * 
 * @author Jerry
 * @E-mail jerry.ma@bstek.com
 * @version 2013-8-26下午2:43:50
 */
@Component
public class MsgContainer {
	private static Log log = LogFactory.getLog(MsgContainer.class);
	private Socket msgSocket;
	private DataInputStream in;
	private DataOutputStream out;

	public DataInputStream getSocketDIS() {
		if (in == null || null == msgSocket || msgSocket.isClosed() || !msgSocket.isConnected()) {
			try {
				in = new DataInputStream(getSocketInstance().getInputStream());
			} catch (IOException e) {
				in = null;
			}
		}
		return in;
	}

	public DataOutputStream getSocketDOS() {
		if (out == null || null == msgSocket || msgSocket.isClosed() || !msgSocket.isConnected()) {
			try {
				out = new DataOutputStream(getSocketInstance().getOutputStream());
			} catch (IOException e) {
				out = null;
			}
		}
		return out;
	}

	public Socket getSocketInstance() {
		if (null == msgSocket || msgSocket.isClosed() || !msgSocket.isConnected()) {
			try {
				in = null;
				out = null;
				msgSocket = new Socket(Configure.getString("ismgIp"), Integer.parseInt(Configure.getString("ismgPort")));
				msgSocket.setKeepAlive(true);
				// msgSocket.setSoTimeout(Integer.parseInt(Configure.getString("timeOut")));
				in = getSocketDIS();
				out = getSocketDOS();
				int count = 0;
				boolean result = connectISMG();
				while (!result) {
					count++;
					result = connectISMG();
					if (count >= (Integer.parseInt(Configure.getString("connectCount")) - 1)) {
						break;
					}
				}
			} catch (UnknownHostException e) {
				log.error("Socket链接短信网关端口号不正确： " + e.getMessage());
			} catch (IOException e) {
				log.error("Socket链接短信网关失败： " + e.getMessage());
			}
		}
		return msgSocket;
	}

	/**
	 * 创建Socket链接后请求链接ISMG
	 * 
	 * @return
	 */
	private boolean connectISMG() {
		log.info("请求连接到ISMG...");
		MsgConnect connect = new MsgConnect();
		connect.setTotalLength(12 + 6 + 16 + 1 + 4);
		connect.setCommandId(MsgCommand.CMPP_CONNECT);
		connect.setSequenceId(MsgUtils.getSequence());
		connect.setSourceAddr(Configure.getString("user"));// 源地址，此处为SP_ID，即SP的企业代码
		String timestamp = MsgUtils.getTimestamp();
		connect.setAuthenticatorSource(MsgUtils.getAuthenticatorSource(Configure.getString("user"), Configure.getString("pwd"), timestamp));
		connect.setTimestamp(Integer.parseInt(timestamp));
		connect.setVersion((byte) 0x20);
		List<byte[]> dataList = new ArrayList<byte[]>();
		dataList.add(connect.toByteArray());
		CmppSender sender = new CmppSender(dataList, getSocketDOS(), getSocketDIS());
		try {
			boolean success = sender.start();
			if (success) {
				log.info("请求连接到ISMG...连接成功！");
			} else {
				log.info("请求连接到ISMG...连接失败！");
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			try {
				out.close();
			} catch (IOException e1) {
				out = null;
			}
			return false;
		}

	}

	/**
	 * 发送短信
	 * 
	 * @param msg
	 * @param cusMsisdn
	 * @return
	 */
	public boolean sendMsg(String msg, String cusMsisdn) {
		try {
			if (msg.getBytes("utf-8").length < 140) {
				boolean result = sendShortMsg(msg, cusMsisdn);
				int count = 0;
				while (!result) {
					count++;
					result = sendShortMsg(msg, cusMsisdn);
					if (count >= (Integer.parseInt(Configure.getString("connectCount")) - 1)) {// 如果再次连接次数超过两次则终止连接
						break;
					}
				}
				return result;
			} else {// 长短信
				boolean result = sendLongMsg(msg, cusMsisdn);
				int count = 0;
				while (!result) {
					count++;
					result = sendLongMsg(msg, cusMsisdn);
					if (count >= (Integer.parseInt(Configure.getString("connectCount")) - 1)) {// 如果再次连接次数超过两次则终止连接
						break;
					}
				}
				return result;
			}
		} catch (Exception e) {
			try {
				out.close();
			} catch (IOException e1) {
				out = null;
			}
			return false;
		}
	}

	private boolean sendShortMsg(String msg, String cusMsisdn) {
		try {
			int seq = MsgUtils.getSequence();
			byte[] msgByte = msg.getBytes("gb2312");
			MsgSubmit submit = new MsgSubmit();
			// 12+8+1+1+1+1+10+1+21+1+1+1+6+2+6+17+17+21+1+21+1+8
			submit.setTotalLength(159 + msgByte.length);
			submit.setCommandId(MsgCommand.CMPP_SUBMIT);
			submit.setSequenceId(seq);
			submit.setPkTotal((byte) 0x01);// 相同Msg_Id的信息总条数，从1开始
			submit.setPkNumber((byte) 0x01);// 相同Msg_Id的信息序号，从1开始
			submit.setRegisteredDelivery((byte) 0x01);// 是否要求返回状态确认报告 0不需要 1需要
			submit.setMsgLevel((byte) 0x01);// 信息级别
			submit.setFeeUserType((byte) 0x02);// 计费用户类型字段
			submit.setFeeTerminalId("");
			submit.setTpPId((byte) 0x00);// GSM协议类型
			submit.setTpUdhi((byte) 0x00);// GSM协议类型
			submit.setMsgFmt((byte) 0x0f);// 信息格式
			submit.setMsgSrc(Configure.getString("spId"));// 信息内容来源（SP的企业代码）
			submit.setSrcId(Configure.getString("serviceId"));// 源号码，SP的服务代码或前缀为服务代码的长号码,
																// 网关将该号码完整的填到SMPP协议Submit_SM消息相应的source_addr字段，
			submit.setDestTerminalId(cusMsisdn);// 接受短信的MSISDN号码
			submit.setMsgLength((byte) msgByte.length);
			submit.setMsgContent(msgByte);

			List<byte[]> dataList = new ArrayList<byte[]>();
			dataList.add(submit.toByteArray());
			CmppSender sender = new CmppSender(dataList, getSocketDOS(), getSocketDIS());
			log.info("向手机号码：" + cusMsisdn + "下发短短信，序列号为:" + seq);
			boolean success = sender.start();
			if (success) {
				log.info("发送成功：" + cusMsisdn);
			} else {
				log.info("发送失败：" + cusMsisdn);
			}
			return true;
		} catch (Exception e) {
			try {
				out.close();
			} catch (IOException e1) {
				out = null;
			}
			log.error("发送短短信" + e.getMessage());
			return false;
		}
	}

	/**
	 * 发送长短信
	 * 
	 * @return
	 */
	private boolean sendLongMsg(String msg, String cusMsisdn) {
		try {
			byte[] allByte = msg.getBytes("iso-10646-ucs-2");
			// byte[] allByte=msg.getBytes("UTF-16BE");
			List<byte[]> dataList = new ArrayList<byte[]>();
			int msgLength = allByte.length;
			int maxLength = 140;
			int msgSendCount = msgLength % (maxLength - 6) == 0 ? msgLength / (maxLength - 6) : msgLength / (maxLength - 6) + 1;
			// 短信息内容头拼接
			byte[] msgHead = new byte[6];
			Random random = new Random();
			random.nextBytes(msgHead); // 为了随机填充msgHead[3]
			msgHead[0] = 0x05;
			msgHead[1] = 0x00;
			msgHead[2] = 0x03;
			msgHead[4] = (byte) msgSendCount;
			msgHead[5] = 0x01;
			int seqId = MsgUtils.getSequence();
			for (int i = 0; i < msgSendCount; i++) {
				// msgHead[3]=(byte)MsgUtils.getSequence();
				msgHead[5] = (byte) (i + 1);
				byte[] needMsg = null;
				// 消息头+消息内容拆分
				if (i != msgSendCount - 1) {
					int start = (maxLength - 6) * i;
					int end = (maxLength - 6) * (i + 1);
					needMsg = MsgUtils.getMsgBytes(allByte, start, end);
				} else {
					int start = (maxLength - 6) * i;
					int end = allByte.length;
					needMsg = MsgUtils.getMsgBytes(allByte, start, end);
				}
				int subLength = needMsg.length + msgHead.length;
				byte[] sendMsg = new byte[needMsg.length + msgHead.length];
				System.arraycopy(msgHead, 0, sendMsg, 0, 6);
				System.arraycopy(needMsg, 0, sendMsg, 6, needMsg.length);
				MsgSubmit submit = new MsgSubmit();
				// 12+8+1+1+1+1+10+1+21+1+1+1+6+2+6+17+17+21+1+21+1+8
				submit.setTotalLength(159 + subLength);
				submit.setCommandId(MsgCommand.CMPP_SUBMIT);
				submit.setSequenceId(seqId);
				submit.setPkTotal((byte) msgSendCount);
				submit.setPkNumber((byte) (i + 1));
				submit.setRegisteredDelivery((byte) 0x00);
				submit.setMsgLevel((byte) 0x01);
				submit.setFeeUserType((byte) 0x02);
				submit.setFeeTerminalId("");
				submit.setTpPId((byte) 0x00);
				submit.setTpUdhi((byte) 0x01);
				submit.setMsgFmt((byte) 0x08);
				submit.setMsgSrc(Configure.getString("spId"));
				submit.setSrcId(Configure.getString("spCode"));
				submit.setDestTerminalId(cusMsisdn);
				submit.setMsgLength((byte) subLength);
				submit.setMsgContent(sendMsg);
				dataList.add(submit.toByteArray());
			}
			CmppSender sender = new CmppSender(dataList, getSocketDOS(), getSocketDIS());
			sender.start();
			log.info("向" + cusMsisdn + "下发长短信，序列号为:" + seqId);
			return true;
		} catch (Exception e) {
			try {
				out.close();
			} catch (IOException e1) {
				out = null;
			}
			log.error("发送长短信" + e.getMessage());
			return false;
		}
	}

	/**
	 * 拆除与ISMG的链接
	 * 
	 * @return
	 */
	public boolean cancelISMG() {
		try {
			MsgHead head = new MsgHead();
			head.setTotalLength(12);// 消息总长度，级总字节数:4+4+4(消息头)+6+16+1+4(消息主体)
			head.setCommandId(MsgCommand.CMPP_TERMINATE);
			head.setSequenceId(MsgUtils.getSequence());

			List<byte[]> dataList = new ArrayList<byte[]>();
			dataList.add(head.toByteArray());
			CmppSender sender = new CmppSender(dataList, getSocketDOS(), getSocketDIS());
			sender.start();
			getSocketInstance().close();
			out.close();
			in.close();
			return true;
		} catch (Exception e) {
			try {
				out.close();
				in.close();
			} catch (IOException e1) {
				in = null;
				out = null;
			}
			log.error("拆除与ISMG的链接" + e.getMessage());
			return false;
		}
	}

	/**
	 * 链路检查
	 * 
	 * @return
	 */
	public boolean activityTestISMG() {
		try {
			MsgHead head = new MsgHead();
			head.setTotalLength(12);
			head.setCommandId(MsgCommand.CMPP_ACTIVE_TEST);
			head.setSequenceId(MsgUtils.getSequence());

			List<byte[]> dataList = new ArrayList<byte[]>();
			dataList.add(head.toByteArray());
			CmppSender sender = new CmppSender(dataList, getSocketDOS(), getSocketDIS());
			sender.start();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			try {
				out.close();
			} catch (Exception e2) {
				out = null;
			}
			log.error("链路检查" + e.getMessage());
			return false;
		}
	}
}
