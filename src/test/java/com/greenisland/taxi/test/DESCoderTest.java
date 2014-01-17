package com.greenisland.taxi.test;

import org.junit.Test;

import com.greenisland.taxi.security.utils.DES;

/**
 * 
 * @author Jerry
 * @E-mail jerry.ma@bstek.com
 * @version 2013-10-28上午11:59:57
 */
public class DESCoderTest {
	private String key = "00019186";
	private String token = "65ca7be6-f844-4494-bec9-16941ca5a930";
	private String uid = "646794ba-3fb8-475f-81e6-5fe7ef5efc76";

	@Test
	public void test() throws Exception {
		System.out.println("DES key：\n" + key);
		System.out.println("key length="+key.length());
		String data = token+","+uid+","+"13818873685";
		String result = DES.encryptDES(data, key);
		System.out.println("DES 加密后：\n" + result);
		String data1 = DES.decryptDES(result, key);
		System.out.println("DES 解密后：\n" + data1);

	}
}
