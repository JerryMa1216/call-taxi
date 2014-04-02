package com.greenisland.taxi.common.utils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;

import com.bstek.dorado.core.Configure;

public class HttpClientUtil {
	/**
	 * 通过HttpURLConnection模拟post表单提交
	 * 
	 * @param path
	 * @param params
	 *            例如"name=zhangsan&age=21"
	 * @return
	 * @throws Exception
	 */
	public static boolean sendPostRequestByForm(String phoneNumber, String message) throws Exception {
		URL url = new URL(Configure.getString("sendUrl"));
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("POST");// 提交模式
		// conn.setConnectTimeout(10000);//连接超时 单位毫秒
		// conn.setReadTimeout(2000);//读取超时 单位毫秒
		conn.setDoOutput(true);// 是否输入参数
		String params = "phoneNumber=" + phoneNumber + "&message=" + URLEncoder.encode(message, "UTF-8");
		byte[] bypes = params.toString().getBytes();
		conn.getOutputStream().write(bypes);// 输入参数
		InputStream inStream = conn.getInputStream();
		String returnData = new String(StreamTool.readInputStream(inStream));
		String flag = returnData.substring(returnData.indexOf(":") + 1, returnData.lastIndexOf("}"));
		if (flag.equals("true")) {
			return true;
		}
		return false;
	}

	public static void main(String[] args) throws Exception{
		String m = "中国";
		System.out.println(URLEncoder.encode(m, "utf-8"));
		String encodeM = URLEncoder.encode(m, "utf-8");
		System.out.println(URLDecoder.decode(encodeM, "utf-8"));
	}

}
