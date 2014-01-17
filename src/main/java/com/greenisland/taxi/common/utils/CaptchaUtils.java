package com.greenisland.taxi.common.utils;

import java.util.Random;

/**
 * 验证码生成工具类
 * 
 * @author Jerry
 * @E-mail jerry.ma@bstek.com
 * @version 2013-8-5下午2:29:57
 */
public class CaptchaUtils {
	public static final Integer CAPTCHA_COUNT = 3;

	public static String getCaptcha() {
		Random random = new Random();
		String captcha = "";
		for (int i = 0; i < 4; i++) {
			String rand = String.valueOf(random.nextInt(10));
			captcha += rand;
		}
		return captcha;
	}
	public static void main(String[] args) {
		System.out.println(getCaptcha());
	}
}
