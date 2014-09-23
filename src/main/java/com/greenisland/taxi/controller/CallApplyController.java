package com.greenisland.taxi.controller;

import java.io.PrintWriter;
import java.text.DateFormat;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.greenisland.taxi.common.constant.ApplicationState;
import com.greenisland.taxi.common.constant.CommentState;
import com.greenisland.taxi.common.constant.ResponseState;
import com.greenisland.taxi.common.constant.TradeState;
import com.greenisland.taxi.common.utils.TCPUtils;
import com.greenisland.taxi.domain.CallApplyInfo;
import com.greenisland.taxi.domain.CommentInfo;
import com.greenisland.taxi.domain.CompanyInfo;
import com.greenisland.taxi.domain.LocationInfo;
import com.greenisland.taxi.domain.TaxiInfo;
import com.greenisland.taxi.domain.UserInfo;
import com.greenisland.taxi.gateway.gps.GpsService;
import com.greenisland.taxi.gateway.gps.SyncClient;
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
	// test
	@Resource
	private GpsService gpsService;
	@Resource
	private UserInfoService userInfoService;
	@Resource
	private LocationInfoService locationInfoService;
	@Resource
	private CallApplyInfoService callApplyInfoService;
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
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		String applyId = "";
		HashMap<String, Object> map = new HashMap<String, Object>();
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:dd"));
		// Map<String, Object> mapCall = null;// 调用接口返回值
		// 根据用户手机号获取用户信息
		UserInfo userInfo = userInfoService.getUserInfoByPhoneNumber(phoneNumber);
		// 爽约次数
		int breakPromisecount = userInfo.getBreakPromiseCount();
		// 爽约时间
		Date breakPromiseDate = userInfo.getBreakPromissDate();
		boolean callFlag = true;
		// 爽约次数不为0
		if (breakPromisecount != 0) {
			Date nowDate = df.parse(df.format(new Date()));
			breakPromiseDate = df.parse(df.format(breakPromiseDate));
			long diff = nowDate.getTime() - breakPromiseDate.getTime();
			long days = diff / (1000 * 60 * 60 * 24);
			// 爽约次数1次，冻结一周
			if (breakPromisecount == 1) {
				if (days < 8) {
					callFlag = false;
				}
			} else if (breakPromisecount == 2) {
				if (days < 15) {
					callFlag = false;
				}
			} else {
				// 爽约次数为3次永久冻结
				callFlag = false;
			}
		}
		// 是否可以叫车
		if (callFlag) {
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
			applyInfo.setEndLocation("默认目的地");// 目的地
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
			applyId = callApplyInfoService.saveCallApplyInfo(applyInfo);

			// 调用GPS平台接口发送打车请求，name为推送中的userId，age为推送中的channelId
			String requestMsg = TCPUtils.getCallApply(applyInfo, applyId + "-" + mechineType + "-" + name + "-" + age + "-" + callType, location,
					userInfo);
			try {
				// syncClient.sendMessage(requestMsg);
				gpsService.sendCallMessage(requestMsg);
				// 叫车请求发送成功
				map.put("state", 0);
				map.put("message", "OK");
				map.put("date", new Date());
				map.put("data", applyId);
			} catch (Exception e) {
				e.printStackTrace();
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
			map.put("state", 2);
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
			log.error("叫车请求异常>>" + e.getMessage());
		}
		// 开启一个倒计时线程，时间到将新增申请置位无效
		new CallApplyThread(callApplyInfoService, applyId, gpsService);
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
	@Transactional
	public void cancelCall(@RequestParam String applyId, @RequestParam String cancelReason, @RequestParam String cancelContent,
			@RequestParam String uid, HttpServletResponse response) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:dd"));
		CallApplyInfo applyInfo = callApplyInfoService.getCallApplyInfo(applyId);
		try {
			if (applyInfo != null) {
				// 司机已抢答，用户取消将订单有效状态置为无效
				applyInfo.setDeleteFlag("N");
				applyInfo.setState(ApplicationState.INVALIDATION);
			 	callApplyInfoService.updateApplyInfo(applyInfo);
				UserInfo userInfo = userInfoService.getUserInfoById(uid);
				userInfo.setBreakPromissDate(new Date());
				int count = userInfo.getBreakPromiseCount();
				userInfo.setBreakPromiseCount(count++);
				userInfo.setUpdateDate(new Date());
				this.userInfoService.updateUserInfo(userInfo);
				gpsService.sendMessage(TCPUtils.getCancelCall(applyInfo.getId()));
			}
			map.put("state", 0);
			map.put("message", "OK");
			map.put("date", new Date());
			map.put("data", null);
		} catch (Exception e) {
			log.error(e.getMessage());
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
			log.error("手动取消叫车请求异常>>" + e.getMessage());
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
	@Transactional
	public void cancel(@RequestParam String applyId, @RequestParam String uid, HttpServletResponse response) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:dd"));
		CallApplyInfo applyInfo = callApplyInfoService.getCallApplyInfoNoResponse(applyId);
		try {
			if (applyInfo != null) {
				applyInfo.setDeleteFlag("Y");
				applyInfo.setState(ApplicationState.INVALIDATION);
				callApplyInfoService.updateApplyInfo(applyInfo);
				gpsService.sendMessage(TCPUtils.getCancelCall(applyInfo.getId()));
			}
			map.put("state", 0);
			map.put("message", "OK");
			map.put("date", new Date());
			map.put("data", null);
		} catch (Exception e) {
			log.error(e.getMessage());
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
			log.error("系统自动取消叫车请求异常>>" + e.getMessage());
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
		map.put("state", "0");
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
			log.error("查询历史订单异常>>" + e.getMessage());
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
		int orderCount = 0;
		int niceCount = 0;
		TaxiInfo taxiInfo = new TaxiInfo();
		CompanyInfo companyInfo = new CompanyInfo();
		List<CallApplyInfo> applies = new ArrayList<CallApplyInfo>();
		if (applyInfo != null) {
			String taxiId = applyInfo.getTaxiId();
			if (StringUtils.hasText(taxiId)) {
				taxiInfo = taxiInfoService.getTaxiInfoById(taxiId);
				companyInfo = companyInfoService.getCompanyById(taxiInfo.getCompanyId());
				// 根据出租车id查询订单信息，好评数
				niceCount = callApplyInfoService.getNiceCount(taxiId);
				applies = callApplyInfoService.getApplyInfoByTaxiId(taxiId);
			}
			orderCount = applies != null && applies.size() > 0 ? applies.size() : 0;
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
			log.error("IOS设备获取推送详情异常>>" + e.getMessage());
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
	@Transactional
	public void commentCall(@RequestParam String applyId, @RequestParam String level, @RequestParam String content, HttpServletResponse response) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:dd"));
		// 根据订单id获取订单信息
		CallApplyInfo applyInfo = callApplyInfoService.getCallApplyInfoById(applyId);
		try {
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
					// 评价级别小于4代表用户已上车
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
		} catch (Exception e) {
			log.error(e.getMessage());
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
			log.error("评价司机异常>>" + e.getMessage());
		}
	}

	private Map<String, Object> convertRecordMap(CallApplyInfo applyInfo, TaxiInfo taxiInfo, int orderCount, int niceCount, String companyName) {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("id", applyInfo.getId());
		returnMap.put("startLocation", applyInfo.getStartLocation());
		returnMap.put("endLocation", applyInfo.getEndLocation());
		returnMap.put("callTime", applyInfo.getCallTime());
		// 1,已抢答 2，等待抢答 0，已关闭
		returnMap.put("responseState", applyInfo.getResponseState());
		returnMap.put("isComment", applyInfo.getIsComment());
		returnMap.put("monitorCount", applyInfo.getMonitorCount());
		returnMap.put("longitude", taxiInfo != null ? taxiInfo.getLongitude() : null);
		returnMap.put("latitude", taxiInfo != null ? taxiInfo.getLatitude() : null);
		returnMap.put("taxiId", taxiInfo != null ? taxiInfo.getId() : null);
		returnMap.put("orderCount", orderCount);
		returnMap.put("niceCount", niceCount);
		returnMap.put("taxiPlateNumber", taxiInfo != null ? taxiInfo.getTaxiPlateNumber() : null);
		returnMap.put("driverName", taxiInfo != null ? taxiInfo.getDriverName() : null);
		returnMap.put("driverPhoneNumber", taxiInfo != null ? taxiInfo.getDirverPhoneNumber() : null);
		returnMap.put("company", companyName);
		returnMap.put("callType", applyInfo.getCallType());
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
