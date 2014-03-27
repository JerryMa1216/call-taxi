package com.greenisland.taxi.controller;

import java.io.PrintWriter;
import java.security.interfaces.RSAPrivateKey;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import sun.misc.BASE64Decoder;

import com.bstek.dorado.core.Configure;
import com.greenisland.taxi.common.constant.UserState;
import com.greenisland.taxi.common.utils.CaptchaUtils;
import com.greenisland.taxi.domain.EquipmentInfo;
import com.greenisland.taxi.domain.FeedBack;
import com.greenisland.taxi.domain.ReturnObject;
import com.greenisland.taxi.domain.UserInfo;
import com.greenisland.taxi.gateway.cmpp.util.MsgContainer;
import com.greenisland.taxi.manager.EquipmentInfoService;
import com.greenisland.taxi.manager.FeedbackService;
import com.greenisland.taxi.manager.UserInfoService;
import com.greenisland.taxi.security.utils.DES;
import com.greenisland.taxi.security.utils.RSA;

/**
 * 系统登陆，获取验证码操作
 * 
 * @author Jerry
 * @E-mail jerry.ma@bstek.com
 * @version 2013-10-21下午9:08:15
 */
@Controller
public class LoginController {
	private static Logger log = Logger.getLogger(LoginController.class.getName());
	@Resource
	private EquipmentInfoService equipmentInfoService;
	@Resource
	private UserInfoService userInfoService;
	@Resource
	private MsgContainer msgContainer;
	@Resource
	private FeedbackService feedbackService;

	/**
	 * 获取短信验证码
	 * 
	 * @param phoneNumber
	 * @param equipmentId
	 *            设备唯一标识
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/get_captcha", method = RequestMethod.GET)
	public String getCaptcha(@RequestParam String phoneNumber, @RequestParam String equipmentId, HttpServletRequest request,
			HttpServletResponse response) {
		boolean flag = false;
		HashMap<String, Object> map = new HashMap<String, Object>();
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:dd"));
		// 根据设备id获取设备数据
		EquipmentInfo equipmentInfo = equipmentInfoService.getEquipmentById(equipmentId);
		// 根据用户手机号获取用户数据
		UserInfo userInfo = userInfoService.getUserInfoByPhoneNumber(phoneNumber);
		// 短信内容
		StringBuilder message = new StringBuilder();
		// 生成短信验证码
		String captchaCode = CaptchaUtils.getCaptcha();
		// 先判断设备当天获取验证码次数是否超过，超过即不允许用户再次获取验证码。
		if (equipmentInfo != null) {
			if (equipmentInfo.getRequestCaptchaCount() < Integer.parseInt(Configure.getString("count"))) {
				// 判断用户是否为新用户
				if (userInfo != null) {
					Integer capCount = userInfo.getRequestCapCount();
					if (capCount < Integer.parseInt(Configure.getString("count"))) {
						try {
							message.append("余杭的士，登陆验证码为： " + captchaCode);
							flag = this.msgContainer.sendMsg(message.toString(), phoneNumber);
							if (flag) {
								userInfo.setCaptcha(captchaCode);
								userInfo.setActivateState(UserState.ACTIVATED);
								userInfo.setRequestCapCount(capCount + 1);
								this.userInfoService.updateUserInfo(userInfo);
								equipmentInfo.setRequestCaptchaCount(equipmentInfo.getRequestCaptchaCount() + 1);
								this.equipmentInfoService.update(equipmentInfo);
								map.put("state", "0");
								map.put("message", "成功");
								map.put("date", new Date());
								map.put("data", null);
							}
						} catch (Exception e) {
							log.error("系统异常>>" + e.getMessage());
							map.put("state", "1");
							map.put("message", "异常失败");
							map.put("date", new Date());
							map.put("data", null);
						}
					} else {
						map.put("state", "2");
						map.put("message", "手机号获取验证码次数过多，请明天再试。");
						map.put("date", new Date());
						map.put("data", null);
					}
				} else {
					// 新用户
					try {
						message.append("余杭的士，登陆验证码为： " + captchaCode);
						flag = this.msgContainer.sendMsg(message.toString(), phoneNumber);
						if (flag) {
							UserInfo newUser = new UserInfo();
							newUser.setPhoneNumber(phoneNumber);
							newUser.setCaptcha(captchaCode);
							newUser.setRequestCapCount(1);
							newUser.setCreateDate(new Date());
							newUser.setUserName(phoneNumber);
							newUser.setActivateState(UserState.ACTIVATED);
							this.userInfoService.saveUserInfo(newUser);
							equipmentInfo.setRequestCaptchaCount(equipmentInfo.getRequestCaptchaCount() + 1);
							this.equipmentInfoService.update(equipmentInfo);
							map.put("state", "0");
							map.put("message", "成功");
							map.put("date", new Date());
							map.put("data", null);
						}
					} catch (Exception e) {
						log.error("系统异常>>" + e.getMessage());
						map.put("state", "1");
						map.put("message", "异常失败");
						map.put("date", new Date());
						map.put("data", null);
					}
				}
			} else {
				map.put("state", "2");
				map.put("message", "设备获取验证码次数过多，请明天再试。");
				map.put("date", new Date());
				map.put("data", null);
			}
		} else {
			// 新设备
			EquipmentInfo newEquipInfo = new EquipmentInfo();
			if (userInfo != null) {
				Integer capCount = userInfo.getRequestCapCount();
				if (capCount < Integer.parseInt(Configure.getString("count"))) {
					try {
						message.append("余杭的士，登陆验证码为： " + captchaCode);
						flag = this.msgContainer.sendMsg(message.toString(), phoneNumber);
						if (flag) {
							userInfo.setCaptcha(captchaCode);
							userInfo.setActivateState(UserState.ACTIVATED);
							userInfo.setRequestCapCount(capCount + 1);
							this.userInfoService.updateUserInfo(userInfo);
							newEquipInfo.setEquipmentId(equipmentId);
							newEquipInfo.setRequestCaptchaCount(1);
							this.equipmentInfoService.save(newEquipInfo);
							map.put("state", "0");
							map.put("message", "成功");
							map.put("date", new Date());
							map.put("data", null);
						}
					} catch (Exception e) {
						log.error("系统异常>>" + e.getMessage());
						map.put("state", "1");
						map.put("message", "异常失败");
						map.put("date", new Date());
						map.put("data", null);
					}
				} else {
					map.put("state", "2");
					map.put("message", "手机号获取验证码次数过多，请明天再试。");
					map.put("date", new Date());
					map.put("data", null);
				}
			} else {
				try {
					message.append("余杭的士，登陆验证码为： " + captchaCode);
					flag = this.msgContainer.sendMsg(message.toString(), phoneNumber);
					if (flag) {
						UserInfo newUser = new UserInfo();
						newUser.setPhoneNumber(phoneNumber);
						newUser.setCaptcha(captchaCode);
						newUser.setRequestCapCount(1);
						newUser.setCreateDate(new Date());
						newUser.setUserName(phoneNumber);
						newUser.setActivateState(UserState.ACTIVATED);
						this.userInfoService.saveUserInfo(newUser);
						newEquipInfo.setEquipmentId(equipmentId);
						newEquipInfo.setRequestCaptchaCount(1);
						this.equipmentInfoService.save(newEquipInfo);
						map.put("state", "0");
						map.put("message", "成功");
						map.put("date", new Date());
						map.put("data", null);
					}
				} catch (Exception e) {
					log.error("系统异常>>" + e.getMessage());
					map.put("state", "1");
					map.put("message", "异常失败");
					map.put("date", new Date());
					map.put("data", null);
				}
			}
		}
		try {
			map.put("c", captchaCode);
			response.reset();
			response.setCharacterEncoding("UTF-8");
			response.setContentType("text/json");
			PrintWriter pw = response.getWriter();
			pw.write(objectMapper.writeValueAsString(map));
			pw.flush();
			pw.close();
		} catch (Exception e) {
			log.error("系统异常>>" + e.getMessage());
		}
		return null;
	}

	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public void login(@RequestParam String sign, @RequestParam String phoneNumber, @RequestParam String captcha, HttpServletResponse response) {
		BASE64Decoder decoder = new BASE64Decoder();
		HashMap<String, Object> map = new HashMap<String, Object>();
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:dd"));
		// DES 加密key
		String key = "";
		// 访问令牌
		String token = UUID.randomUUID().toString().replaceAll("-", "");
		try {
			if (phoneNumber.equals(Configure.getString("testAccount")) || phoneNumber.equals(Configure.getString("testAndroid"))) {
				UserInfo baseUser = this.userInfoService.getUserInfoByPhoneNumber(phoneNumber);
				// 初始化私钥
				RSA.loadPrivateKey(RSA.DEFAULT_PRIVATE_KEY);
				// 私钥
				RSAPrivateKey privateKey = RSA.getPrivateKey();
				byte[] bSign = decoder.decodeBuffer(sign);
				// 根据私钥进行解密
				byte[] decodeSign = RSA.decrypt(privateKey, bSign);
				String outputData = new String(decodeSign);
				key = outputData.split(",")[0];
				// 登陆成功
				baseUser.setKey(key);
				baseUser.setToken(token);
				baseUser.setActivateState(UserState.NON_ACTIVATED);
				this.userInfoService.updateUserInfo(baseUser);
				String returnData = baseUser.getId() + "," + token;
				map.put("state", "0");
				map.put("message", "登陆成功！");
				map.put("date", new Date());
				map.put("data", new ReturnObject(DES.encryptDES(returnData, key)));
			} else {
				// 客户端生成随即字符串，用来做对称加密使用
				UserInfo baseUser = this.userInfoService.getUserInfoByPhoneNumber(phoneNumber);
				if (baseUser != null && baseUser.getActivateState().equals(UserState.ACTIVATED)) {
					if (baseUser.getCaptcha().equals(captcha)) {
						// 初始化私钥
						RSA.loadPrivateKey(RSA.DEFAULT_PRIVATE_KEY);
						// 私钥
						RSAPrivateKey privateKey = RSA.getPrivateKey();
						byte[] bSign = decoder.decodeBuffer(sign);
						// 根据私钥进行解密
						byte[] decodeSign = RSA.decrypt(privateKey, bSign);
						String outputData = new String(decodeSign);
						key = outputData.split(",")[0];
						// 登陆成功
						baseUser.setKey(key);
						baseUser.setToken(token);
						baseUser.setActivateState(UserState.NON_ACTIVATED);
						this.userInfoService.updateUserInfo(baseUser);
						String returnData = baseUser.getId() + "," + token;
						map.put("state", "0");
						map.put("message", "登陆成功！");
						map.put("date", new Date());
						map.put("data", new ReturnObject(DES.encryptDES(returnData, key)));
					} else {
						map.put("state", "1");
						map.put("message", "登陆失败,验证码不正确！");
						map.put("date", new Date());
						map.put("data", new ReturnObject(null));
					}
				} else {
					map.put("state", "2");
					map.put("message", "您的账号已在其他设备上登陆！");
					map.put("date", new Date());
					map.put("data", new ReturnObject(null));
				}
			}
			response.reset();
			response.setCharacterEncoding("UTF-8");
			response.setContentType("text/json");
			PrintWriter pw = response.getWriter();
			pw.write(objectMapper.writeValueAsString(map));
			pw.flush();
			pw.close();
		} catch (Exception e) {
			log.error("系统异常>>" + e.getMessage());
		}
	}

	/**
	 * 意见与反馈
	 * 
	 * @param username
	 * @param content
	 * @param response
	 */
	@RequestMapping(value = "/feedback", method = RequestMethod.POST)
	public void feedback(@RequestParam String username, @RequestParam String content, HttpServletResponse response) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:dd"));
		FeedBack back = new FeedBack();
		try {
			back.setUsername(username);
			back.setContent(content);
			back.setCreateDate(new Date());
			this.feedbackService.save(back);
			map.put("state", "0");
			map.put("message", "反馈成功！");
			map.put("date", new Date());
			map.put("data", new ReturnObject(null));

		} catch (Exception e) {
			log.error("系统异常>>" + e.getMessage());
			map.put("state", "1");
			map.put("message", "反馈失败！");
			map.put("date", new Date());
			map.put("data", new ReturnObject(null));
		}
		try {
			response.reset();
			response.setCharacterEncoding("UTF-8");
			response.setContentType("text/json");
			PrintWriter pw = response.getWriter();
			pw.write(objectMapper.writeValueAsString(map));
			pw.flush();
			pw.close();
		} catch (Exception e) {
			log.error("系统异常>>" + e.getMessage());
		}
	}

	/**
	 * 退出系统，将token令牌置空
	 * 
	 * @param phoneNumber
	 * @param response
	 */
	@RequestMapping(value = "/logout", method = RequestMethod.POST)
	public void logout(@RequestParam String phoneNumber, @RequestParam String uid, HttpServletResponse response) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:dd"));
		UserInfo userInfo = this.userInfoService.getUserInfoById(uid);
		userInfo.setToken(null);
		userInfo.setActivateState(UserState.ACTIVATED);
		userInfo.setUpdateDate(new Date());
		try {
			this.userInfoService.updateUserInfo(userInfo);
			map.put("state", "0");
			map.put("message", "退出成功！");
			map.put("date", new Date());
			map.put("data", new ReturnObject(null));

		} catch (Exception e) {
			log.error("系统异常>>" + e.getMessage());
			map.put("state", "1");
			map.put("message", "退出失败！");
			map.put("date", new Date());
			map.put("data", new ReturnObject(null));
		}
		try {
			response.reset();
			response.setCharacterEncoding("UTF-8");
			response.setContentType("text/json");
			PrintWriter pw = response.getWriter();
			pw.write(objectMapper.writeValueAsString(map));
			pw.flush();
			pw.close();
		} catch (Exception e) {
			log.error("系统异常>>" + e.getMessage());
		}
	}

	/**
	 * 页面跳转
	 * 
	 * @return
	 */
	@RequestMapping(value = "/get_store", method = RequestMethod.GET)
	public ModelAndView getStore() {
		String storeUrl = Configure.getString("storeUrl");
		return new ModelAndView("redirect:" + storeUrl);
	}
}
