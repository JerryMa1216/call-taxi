package com.greenisland.taxi.gateway.cmpp.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author Jerry
 * @E-mail jerry.ma@bstek.com
 * @version 2013-8-26上午11:19:00
 */
public class MsgUtils {
	private static int sequenceId = 0;// 序列编号
	private static Log log = LogFactory.getLog(MsgUtils.class);

	/**
	 * 序列自增
	 * 
	 * @return
	 */
	public synchronized static int getSequence() {
		++sequenceId;
		if (sequenceId > 255) {
			sequenceId = 0;
		}
		return sequenceId;
	}

	/**
	 * 时间戳的明文，由客户端产生，格式为MMddhhmmss，即月日时分秒，10位数字的整形，右对齐
	 * 
	 * @return
	 */
	public static String getTimestamp() {
		DateFormat format = new SimpleDateFormat("MMddhhmmss");
		return format.format(new Date());
	}

	/**
	 * 用于鉴别源地址，其值通过单向MD5 hash计算得出
	 * 
	 * @param spId
	 * @param secret
	 * @param timestamp
	 * @return
	 */
	public static byte[] getAuthenticatorSource(String spId, String secret, String timestamp) {
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			// 用户鉴别源地址
			byte[] data = (spId + "\0\0\0\0\0\0\0\0\0" + secret + timestamp).getBytes();
			return md5.digest(data);
		} catch (Exception e) {
			log.error("SP链接到ISMG拼接AuthenticatorSource失败： " + e.getMessage());
			return null;
		}
	}

	/**
	 * 向流中写入指定字节长度的字符串，不足时补0
	 * 
	 * @param dous
	 *            要写入的流对象
	 * @param s
	 *            要写入的字符串
	 * @param len
	 *            写入长度，不足补0
	 */
	public static void writeString(DataOutputStream dous, String s, int len) {
		try {
			byte[] data = s.getBytes("gb2312");
			if (data.length > len) {
				log.error("向流中写入的字符串超长！要写" + len + " 字符串是： " + s);
			}
			int srcLen = data.length;
			dous.write(data);
			while (srcLen < len) {
				dous.write('\0');
				srcLen++;
			}
		} catch (Exception e) {
			log.error("向流中写入指定字节长度的字符串失败： " + e.getMessage());
		}
	}

	/**
	 * 从流中读取指定长度的字节，转成字符串返回
	 * 
	 * @param ins
	 * @param len
	 * @return
	 */
	public static String readString(DataInputStream ins, int len) {
		byte[] b = new byte[len];
		try {
			ins.read(b);
			String s = new String(b);
			s = s.trim();
			return s;
		} catch (Exception e) {
			return "";
		}
	}

	/**
	 * 截取字节
	 * 
	 * @param msg
	 * @param start
	 * @param end
	 * @return
	 */
	public static byte[] getMsgBytes(byte[] msg, int start, int end) {
		byte[] msgByte = new byte[end - start];
		int j = 0;
		for (int i = start; i < end; i++) {
			msgByte[j] = msg[i];
			j++;
		}
		return msgByte;
	}

	/**
	 * UCS2解码
	 * 
	 * @param src
	 * @return
	 */
	public static String DecodeUCS2(String src) {
		byte[] bytes = new byte[src.length() / 2];
		for (int i = 0; i < src.length(); i += 2) {
			bytes[i / 2] = (byte) (Integer.parseInt(src.substring(i, i + 2), 16));
		}
		String reValue = "";
		try {
			reValue = new String(bytes, "UTF-16BE");
		} catch (UnsupportedEncodingException e) {
			reValue = "";
		}
		return reValue;
	}

	/**
	 * UCS2编码
	 * 
	 * @param src
	 * @return
	 */
	public static String EncodeUCS2(String src) {
		byte[] bytes;
		try {
			bytes = src.getBytes("UTF-16BE");
		} catch (UnsupportedEncodingException e) {
			bytes = new byte[0];
		}
		StringBuffer reValue = new StringBuffer();
		StringBuffer tem = new StringBuffer();
		for (int i = 0; i < bytes.length; i++) {
			tem.delete(0, tem.length());
			tem.append(Integer.toHexString(bytes[i] & 0xFF));
			if (tem.length() == 1) {
				tem.insert(0, '0');
			}
			reValue.append(tem);
		}
		return reValue.toString().toUpperCase();
	}
}
