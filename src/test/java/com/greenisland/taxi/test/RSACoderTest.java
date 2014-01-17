package com.greenisland.taxi.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Before;
import org.junit.Test;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import com.greenisland.taxi.security.utils.RSA;

/**
 * 
 * @author Jerry
 * @E-mail jerry.ma@bstek.com
 * @version 2013-10-20下午2:02:42
 */
public class RSACoderTest {

	private RSAPrivateKey privateKey;
	private RSAPublicKey publicKey;

	@Before
	public void initKey() throws Exception {
		RSA.loadPrivateKey(RSA.DEFAULT_PRIVATE_KEY);
		RSA.loadPublicKey(RSA.DEFAULT_PUBLIC_KEY);
		privateKey = RSA.getPrivateKey();
		publicKey = RSA.getPublicKey();
	}

	@Test
	public void test() throws Exception {
		BASE64Encoder encoder = new BASE64Encoder();
		BASE64Decoder decoder = new BASE64Decoder();
		System.err.println("\n----公钥加密----私钥解密------");
		String inputStr1 = "20120402,13818873685,1234";
		byte[] data1 = inputStr1.getBytes();
		System.err.println("原文:\n" + inputStr1);
		System.out.println("私钥：\n" + new String(encoder.encode(privateKey.getEncoded())));
		System.out.println("公钥:\n" + new String(encoder.encode(publicKey.getEncoded())));
		System.err.println("data1:\n" + data1);
		// 加密
		byte[] encodedData1 = RSA.encrypt(publicKey, data1);
		System.err.println("加密后：\n" + encodedData1);
		System.out.println("加密字符串：\n");
		String result = new String(encoder.encode((encodedData1)));
		System.out.println(result);
		// 解密
		byte[] decodeData1 = RSA.decrypt(privateKey, decoder.decodeBuffer(result));
		String outputStr1 = new String(decodeData1);
		System.err.println("解密后：\n" + outputStr1);
		String url = "http://localhost:8080/taxi/rest/1.0/taxi/login";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("sign", result);
		map.put("phoneNumber", "13818873685");
		map.put("captcha", "1234");
		String returnData =sendPost(url, map);
		System.out.println(returnData);
	}

	@SuppressWarnings("rawtypes")
	private static String sendPost(String url, Map<String, Object> params) {
		String result = "";
		try {
			URL httpurl = new URL(url);
			HttpURLConnection httpConn = (HttpURLConnection) httpurl.openConnection();
			httpConn.setDoOutput(true);
			httpConn.setDoInput(true);
			StringBuilder parameters = new StringBuilder();
			for (Iterator iter = params.entrySet().iterator(); iter.hasNext();) {
				Entry element = (Entry) iter.next();
				parameters.append(element.getKey().toString());
				parameters.append("=");
				parameters.append(URLEncoder.encode(element.getValue().toString(), "GBK"));
				parameters.append("&");
			}
			if (parameters.length() > 0) {
				parameters = parameters.deleteCharAt(parameters.length() - 1);
			}
			byte[] b = parameters.toString().getBytes();
			httpConn.getOutputStream().write(b, 0, b.length);
			httpConn.getOutputStream().flush();
			httpConn.getOutputStream().close();
			BufferedReader in = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
			in.close();
			if (httpConn != null) {
				httpConn.disconnect();
			}
		} catch (Exception e) {
			System.out.println("没有结果！" + e);
		}
		return result;
	}

}
