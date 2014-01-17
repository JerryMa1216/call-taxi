package com.greenisland.taxi.controller;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.greenisland.taxi.common.constant.ApplicationState;
import com.greenisland.taxi.common.constant.CommentState;
import com.greenisland.taxi.common.constant.GPSCommand;
import com.greenisland.taxi.common.constant.ResponseState;
import com.greenisland.taxi.common.constant.TradeState;
import com.greenisland.taxi.common.utils.TCPUtils;
import com.greenisland.taxi.domain.CallApplyInfo;
import com.greenisland.taxi.domain.CommentInfo;
import com.greenisland.taxi.domain.CompanyInfo;
import com.greenisland.taxi.domain.LocationInfo;
import com.greenisland.taxi.domain.TaxiInfo;
import com.greenisland.taxi.domain.UserInfo;
import com.greenisland.taxi.gateway.gps.SyncClient;
import com.greenisland.taxi.gateway.gps.resolver.MessageHandler;
import com.greenisland.taxi.manager.CallApplyInfoService;
import com.greenisland.taxi.manager.CommentInfoService;
import com.greenisland.taxi.manager.CompanyInfoService;
import com.greenisland.taxi.manager.LocationInfoService;
import com.greenisland.taxi.manager.TaxiInfoService;
import com.greenisland.taxi.manager.UserInfoService;

/**
 * 订单操作
 * 
 * @author Jerry
 * @E-mail jerry.ma@bstek.com
 * @version 2013-10-30下午4:38:58
 */
@Controller
public class CallApplyController {
	private static Logger log = Logger.getLogger(CallApplyController.class.getName());
	@Resource
	private SyncClient syncClient;
	@Resource
	private UserInfoService userInfoService;
	@Resource
	private LocationInfoService locationInfoService;
	@Resource
	private CallApplyInfoService callApplyInfoService;
	@Resource
	private MessageHandler messageHandler;
	@Resource
	private CommentInfoService commentInfoService;
	@Resource
	private TaxiInfoService taxiInfoService;
	@Resource
	private CompanyInfoService companyInfoService;

	private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	/**
	 * 叫车请求
	 * 
	 * @param phoneNumber
	 * @param callTime
	 * @param callType
	 * @param callScope
	 * @param callDistance
	 * @param mechineType
	 *            设备类型 1，Andriod 2，ios
	 * @param sLoca
	 *            起点位置
	 * @param eLoca
	 *            重点位置
	 * @param longitude
	 * @param latitude
	 * @throws Exception
	 */
	@RequestMapping(value = "/call_taxi", method = RequestMethod.POST)
	public void callTaxi(@RequestParam String phoneNumber, @RequestParam String callTime, @RequestParam String callType,
			@RequestParam String callScope, @RequestParam String callDistance, @RequestParam String mechineType, @RequestParam String sLoca,
			@RequestParam String eLoca, @RequestParam String longitude, @RequestParam String latitude, @RequestParam String name,
			@RequestParam String age, HttpServletResponse response) throws Exception {
		HashMap<String, Object> map = new HashMap<String, Object>();
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:dd"));
		Map<String, Object> mapCall = null;// 调用接口返回值
		// 根据用户手机号获取用户信息
		UserInfo userInfo = userInfoService.getUserInfoByPhoneNumber(phoneNumber);
		// 保存用户叫车位置，方便后台管理系统做数据分析
		LocationInfo location = new LocationInfo();
		location.setCreateDate(new Date());
		location.setGpsLatitude(latitude);
		location.setGpsLongitude(longitude);
		location.setGpsTime(new Date());
		location.setUserId(userInfo.getId());
		locationInfoService.saveLocationInfo(location);
		// 新增打车请求记录
		CallApplyInfo applyInfo = new CallApplyInfo();
		applyInfo.setUserId(userInfo.getId());
		applyInfo.setCallLength(Integer.parseInt(callDistance));// 叫车距离
		applyInfo.setCallScope(Integer.parseInt(callScope));// 叫车范围
		applyInfo.setCallTime(format.parse(callTime));// 叫车时间
		applyInfo.setCallType(callType);// 叫车类型 1即时叫车 2预约叫车
		applyInfo.setEndLocation(eLoca);// 目的地
		applyInfo.setStartLocation(sLoca);// 出发地
		applyInfo.setMechineType(mechineType);// 设备类型
		applyInfo.setCreateDate(new Date());
		applyInfo.setState(ApplicationState.VALIDATION);// 请求状态
		applyInfo.setIsGetOn("0");// 是否上车
		applyInfo.setResponseState(ResponseState.WAIT_RESPONSE);// 是否响应
		applyInfo.setTradeState(TradeState.WAIT_FINISH);// 交易状态
		applyInfo.setMonitorCount(0);// 监控次数
		applyInfo.setIsComment("0");// 是否评价
		applyInfo.setDeleteFlag("N");// 未删除
		applyInfo.setGpsLongitude(longitude);
		applyInfo.setGpsLatitude(latitude);
		String applyId = callApplyInfoService.saveCallApplyInfo(applyInfo);

		// 调用GPS平台接口发送打车请求，name为推送中的userId，age为推送中的channelId
		String requestMsg = TCPUtils.getCallApply(applyInfo, applyId + "-" + mechineType + "-" + name + "-" + age + "-" + callType, location,userInfo);
		boolean flag = syncClient.sendMessage(requestMsg);
		if (flag) {
			// 获取GPS平台返回数据
			String responseData = syncClient.getResult();
			mapCall = messageHandler.handler(responseData);
			String returnData = (String) mapCall.get(GPSCommand.GPS_CALL_RESP + "");
			if (!returnData.equals("ER")) {
				// 叫车请求发送成功
				map.put("state", 0);
				map.put("message", "OK");
				map.put("date", new Date());
				map.put("data", returnData.substring(0, returnData.indexOf("-")));
			} else {
				CallApplyInfo apply = callApplyInfoService.getCallApplyInfoById(applyId);
				apply.setDeleteFlag("Y");
				apply.setState(ApplicationState.INVALIDATION);
				callApplyInfoService.updateApplyInfo(apply);
				map.put("state", 1);
				map.put("message", "ER");
				map.put("date", new Date());
				map.put("data", null);
			}
		} else {
			applyInfo.setId(applyId);
			applyInfo.setDeleteFlag("Y");
			applyInfo.setState("0");
			this.callApplyInfoService.updateApplyInfo(applyInfo);
			map.put("state", 1);
			map.put("message", "ER");
			map.put("date", new Date());
			map.put("data", null);
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
			log.error("=================系统异常>>" + e.getMessage());
		}
		// 开启一个倒计时线程，时间到将新增申请置位无效
		new CallApplyThread(callApplyInfoService, applyId);
	}

	/**
	 * 手动取消即时叫车请求（已抢答）
	 * 
	 * @param applyId
	 * @param cancelReason
	 * @param uid
	 * @param response
	 */
	@RequestMapping(value = "/call_cancel", method = RequestMethod.POST)
	public void cancelCall(@RequestParam String applyId, @RequestParam String cancelReason, @RequestParam String cancelContent,
			@RequestParam String uid, HttpServletResponse response) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:dd"));
		CallApplyInfo applyInfo = callApplyInfoService.getCallApplyInfo(applyId);
		if (applyInfo != null) {
			applyInfo.setDeleteFlag("Y");
			applyInfo.setState(ApplicationState.INVALIDATION);
			callApplyInfoService.updateApplyInfo(applyInfo);
			UserInfo userInfo = userInfoService.getUserInfoById(uid);
			userInfo.setBreakPromissDate(new Date());
			int count = userInfo.getBreakPromiseCount();
			userInfo.setBreakPromiseCount(count++);
			userInfo.setUpdateDate(new Date());
			this.userInfoService.updateUserInfo(userInfo);
		}
		map.put("state", 0);
		map.put("message", "OK");
		map.put("date", new Date());
		map.put("data", null);
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
	 * 手动取消即时叫车请求（未抢答）
	 * 
	 * @param applyId
	 * @param cancelReason
	 * @param uid
	 * @param response
	 */
	@RequestMapping(value = "/cancel", method = RequestMethod.POST)
	public void cancel(@RequestParam String applyId, @RequestParam String uid, HttpServletResponse response) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:dd"));
		CallApplyInfo applyInfo = callApplyInfoService.getCallApplyInfoNoResponse(applyId);
		if (applyInfo != null) {
			applyInfo.setDeleteFlag("Y");
			applyInfo.setState(ApplicationState.INVALIDATION);
			callApplyInfoService.updateApplyInfo(applyInfo);
		}
		map.put("state", 0);
		map.put("message", "OK");
		map.put("date", new Date());
		map.put("data", null);
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
	 * 历史订单信息查询
	 * 
	 * @param uid
	 * @param response
	 */
	@RequestMapping(value = "/query_orders", method = RequestMethod.POST)
	public void queryHistoryOrder(@RequestParam String uid, HttpServletResponse response) throws Exception {
		HashMap<String, Object> map = new HashMap<String, Object>();
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:dd"));
		List<CallApplyInfo> list = callApplyInfoService.queryApplyInfoByUid(uid);
		for (CallApplyInfo apply : list) {
			TaxiInfo taxi = taxiInfoService.getTaxiInfoById(apply.getTaxiId() != null ? apply.getTaxiId() : "");
			if (taxi != null) {
				CompanyInfo company = companyInfoService.getCompanyById(taxi.getCompanyId());
				apply.setTaxiPlateNumber(taxi.getTaxiPlateNumber());
				apply.setDriverName(taxi.getDriverName());
				apply.setDirverPhoneNumber(taxi.getDirverPhoneNumber());
				apply.setCompany(company.getName());
			} else {
				apply.setTaxiPlateNumber("");
				apply.setDriverName("");
				apply.setDirverPhoneNumber("");
				apply.setCompany("");
			}
		}
		map.put("state", 0);
		map.put("message", "OK");
		map.put("date", new Date());
		map.put("data", convertMap(list));
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
	 * IOS设备推送获取详细信息
	 * 
	 * @param applyId
	 * @param response
	 */
	@RequestMapping(value = "/get_record", method = RequestMethod.POST)
	public void getRecordInfo(@RequestParam String applyId, HttpServletResponse response) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:dd"));
		CallApplyInfo applyInfo = callApplyInfoService.getCallApplyInfoById(applyId);
		int orderCount;
		int niceCount;
		if (applyInfo != null) {
			String taxiId = applyInfo.getTaxiId();
			TaxiInfo taxiInfo = taxiInfoService.getTaxiInfoById(taxiId);
			CompanyInfo companyInfo = companyInfoService.getCompanyById(taxiInfo.getCompanyId());
			// 根据出租车id查询订单信息
			List<CallApplyInfo> applies = callApplyInfoService.getApplyInfoByTaxiId(taxiId);
			orderCount = applies != null && applies.size() > 0 ? applies.size() : 0;
			niceCount = callApplyInfoService.getNiceCount(taxiId);
			map.put("state", "0");
			map.put("message", "OK");
			map.put("date", new Date());
			map.put("data", convertRecordMap(applyInfo, taxiInfo, orderCount, niceCount, companyInfo.getName()));
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
	 * 评价司机
	 * 
	 * @param applyId
	 * @param level
	 *            小于5 已上车 ，大于5 未上车
	 * @param content
	 * @param response
	 * @param canceContent
	 *            TODO
	 */
	@RequestMapping(value = "/comment", method = RequestMethod.POST)
	public void commentCall(@RequestParam String applyId, @RequestParam String level, @RequestParam String content, HttpServletResponse response) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:dd"));
		CallApplyInfo applyInfo = callApplyInfoService.getCallApplyInfoById(applyId);
		if (applyInfo != null) {
			CommentInfo comment = commentInfoService.getCommentInfo(applyId);
			if (comment != null) {
				comment.setLevel(Integer.parseInt(level));
				comment.setContent(content);
				comment.setUpdateDate(new Date());
				commentInfoService.updateCommentInfo(comment);
			} else {
				CommentInfo newComment = new CommentInfo();
				newComment.setLevel(Integer.parseInt(level));
				newComment.setContent(content);
				newComment.setCreateDate(new Date());
				newComment.setApplyId(applyId);
				commentInfoService.saveCommentInfo(newComment);
			}
			if (Integer.parseInt(level) < 4) {
				applyInfo.setIsGetOn("1");
			} else {
				applyInfo.setIsGetOn("0");
			}
			applyInfo.setTradeState(TradeState.FINISHED);
			applyInfo.setIsComment(CommentState.COMMENT);
			callApplyInfoService.updateApplyInfo(applyInfo);
			map.put("state", 0);
			map.put("message", "OK");
			map.put("date", new Date());
			map.put("data", null);
		} else {
			map.put("state", 1);
			map.put("message", "ER");
			map.put("date", new Date());
			map.put("data", null);
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

	private Map<String, Object> convertRecordMap(CallApplyInfo applyInfo, TaxiInfo taxiInfo, int orderCount, int niceCount, String companyName) {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("id", applyInfo.getId());
		returnMap.put("startLocation", applyInfo.getStartLocation());
		returnMap.put("endLocation", applyInfo.getEndLocation());
		returnMap.put("callTime", applyInfo.getCallTime());
		returnMap.put("tradeState", applyInfo.getTradeState());
		returnMap.put("isComment", applyInfo.getIsComment());
		returnMap.put("monitorCount", applyInfo.getMonitorCount());
		returnMap.put("longitude", taxiInfo.getLongitude());
		returnMap.put("latitude", taxiInfo.getLatitude());
		returnMap.put("taxiId", taxiInfo.getId());
		returnMap.put("orderCount", orderCount);
		returnMap.put("niceCount", niceCount);
		returnMap.put("taxiPlateNumber", taxiInfo.getTaxiPlateNumber());
		returnMap.put("driverName", taxiInfo.getDriverName());
		returnMap.put("driverPhoneNumber", taxiInfo.getDirverPhoneNumber());
		returnMap.put("company", companyName);
		return returnMap;
	}

	private List<Map<String, Object>> convertMap(List<CallApplyInfo> list) {
		List<Map<String, Object>> returnList = new ArrayList<Map<String, Object>>();
		int orderCount = 0;
		int niceCount = 0;
		for (CallApplyInfo applyInfo : list) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("id", applyInfo.getId());
			map.put("startLocation", applyInfo.getStartLocation());
			map.put("endLocation", applyInfo.getEndLocation());
			map.put("callTime", applyInfo.getCallTime());
			map.put("tradeState", applyInfo.getTradeState());
			map.put("isComment", applyInfo.getIsComment());
			map.put("monitorCount", applyInfo.getMonitorCount());
			map.put("longitude", applyInfo.getGpsLongitude());
			map.put("latitude", applyInfo.getGpsLatitude());
			map.put("taxiId", applyInfo.getTaxiId());
			// 根据出租车id查询订单信息
			List<CallApplyInfo> applies = callApplyInfoService.getApplyInfoByTaxiId(applyInfo.getTaxiId());
			orderCount = applies != null && applies.size() > 0 ? applies.size() : 0;
			niceCount = callApplyInfoService.getNiceCount(applyInfo.getTaxiId());
			map.put("orderCount", orderCount);
			map.put("niceCount", niceCount);
			map.put("taxiPlateNumber", applyInfo.getTaxiPlateNumber());
			map.put("driverName", applyInfo.getDriverName());
			map.put("driverPhoneNumber", applyInfo.getDirverPhoneNumber());
			map.put("company", applyInfo.getCompany());
			returnList.add(map);
		}
		return returnList;
	}
}
