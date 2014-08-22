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

import com.bstek.dorado.core.Configure;
import com.greenisland.taxi.common.constant.GPSCommand;
import com.greenisland.taxi.common.utils.TCPUtils;
import com.greenisland.taxi.domain.CallApplyInfo;
import com.greenisland.taxi.domain.TaxiInfo;
import com.greenisland.taxi.gateway.gps.GpsService;
import com.greenisland.taxi.gateway.gps.SyncClient;
import com.greenisland.taxi.gateway.gps.resolver.MessageHandler;
import com.greenisland.taxi.manager.CallApplyInfoService;
import com.greenisland.taxi.manager.TaxiInfoService;

/**
 * 车辆操作
 * 
 * @author Jerry
 * @E-mail jerry.ma@bstek.com
 * @version 2013-10-29下午5:31:21
 */
@Controller
public class TaxiController {
	private static Logger log = Logger.getLogger(TaxiController.class.getName());
	@Resource
	private SyncClient syncClient;
	// test
	@Resource
	private GpsService gpsService;
	@Resource
	private MessageHandler messageHandler;
	@Resource
	private TaxiInfoService taxiInfoService;
	@Resource
	private CallApplyInfoService applyInfoService;

	/**
	 * 周边车辆查询
	 * 
	 * @param longitude
	 * @param latitude
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/query_count", method = RequestMethod.GET)
	public void queryTaxisCount(@RequestParam String longitude, @RequestParam String latitude, @RequestParam String radius,
			HttpServletResponse response) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:dd"));
		// 持久化用户叫车位置
		Map<String, Object> mapTaxi = null;// 调用接口返回值
		try {
			String requestParam = TCPUtils.getTaxis(longitude, latitude, radius);
			String returnData = gpsService.sendMessage(requestParam);
			mapTaxi = messageHandler.handler(returnData);
			// 周边车辆查询，GPS平台返回的出租车信息
			List<TaxiInfo> taxis = (List<TaxiInfo>) mapTaxi.get(Integer.toString(GPSCommand.GPS_AROUND_TAXIS));
			map.put("state", "0");
			map.put("message", "OK");
			map.put("date", new Date());
			map.put("data", taxis.size());
		} catch (Exception e) {
			log.error("系统异常>>" + e.getMessage());
			map.put("state", "0");
			map.put("message", "OK");
			map.put("date", new Date());
			map.put("data", 0);
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
	 * 周边车辆查询
	 * 
	 * @param longitude
	 * @param latitude
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/query_taxi", method = RequestMethod.GET)
	public void queryTaxis(@RequestParam String longitude, @RequestParam String latitude, HttpServletResponse response) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:dd"));
		// 默认查询半径
		String defaultRadius = Configure.getString("defaultRadius");
		// 持久化用户叫车位置
		Map<String, Object> mapTaxi = null;// 调用接口返回值
		try {
			String requestParam = TCPUtils.getTaxis(longitude, latitude, defaultRadius);
			// syncClient.sendMessage(requestParam);
			// String returnData = syncClient.sendMessage(requestParam);
			String returnData = gpsService.sendMessage(requestParam);
			mapTaxi = messageHandler.handler(returnData);
			// 周边车辆查询，GPS平台返回的出租车信息
			List<TaxiInfo> taxis = (List<TaxiInfo>) mapTaxi.get(Integer.toString(GPSCommand.GPS_AROUND_TAXIS));
			List<TaxiInfo> reTaxis = new ArrayList<TaxiInfo>();
			TaxiInfo tempTaxi = new TaxiInfo();
			if (taxis != null) {
				for (TaxiInfo taxi : taxis) {
					// if (taxi.getIsEmpty().equals("0")) {
					// 验证出租车是否存在
					if (taxiInfoService.validateTaxiExist(taxi.getTaxiPlateNumber())) {
						// 存在，获取出租车出租车历史订单信息
						tempTaxi = taxiInfoService.getTaxiByPlateNumber(taxi.getTaxiPlateNumber());
						taxi.setId(tempTaxi.getId());
						taxi.setCallApplyInfos(tempTaxi.getCallApplyInfos());
						taxi.setBreakPromiseCount(tempTaxi.getBreakPromiseCount());
					} else {
						// 不存在，则历史订单信息为空，爽约次数也为空
						taxi.setCallApplyInfos(null);
						taxi.setBreakPromiseCount(0);
					}
					// 设置返回值
					reTaxis.add(taxi);
					// }
				}
				map.put("state", "0");
				map.put("message", "OK");
				map.put("count", reTaxis.size());
				map.put("date", new Date());
				map.put("data", convertMap(reTaxis));
			} else {
				map.put("state", "0");
				map.put("message", "OK");
				map.put("count", 0);
				map.put("date", new Date());
				map.put("data", null);
			}
		} catch (Exception e) {
			log.error("系统异常>>" + e.getMessage());
			map.put("state", "1");
			map.put("message", "ER");
			map.put("count", 0);
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

	/**
	 * 监控抢到订单的出租车位置信息
	 * 
	 * @param applyId
	 * @param plateNumber
	 * @param response
	 */
	@RequestMapping(value = "/monitor")
	public void monitorTaxi(@RequestParam String applyId, @RequestParam String plateNumber, HttpServletResponse response) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:dd"));
		// 根据申请的id获取申请信息
		CallApplyInfo applyInfo = applyInfoService.getCallApplyInfoById(applyId);
		Map<String, Object> returnMap = new HashMap<String, Object>();
		Map<String, Object> mapReturn = null;
		try {
			String responseData = null;
			// 响应消息体如果不为1005，则一直去1005消息体
			int count = 0;
			boolean flagOk = false;
			while (count < 5) {
				responseData = gpsService.sendMessage(TCPUtils.getMonitorMessage(applyId, plateNumber));
				String msg1 = responseData.substring(2);
				String msg2 = msg1.substring(0, msg1.indexOf(">"));
				// 消息id
				String msgId = msg2.substring(0, 4);
				if (msgId.equals(Integer.toString(GPSCommand.GPS_TAXI_MONITER))) {
					flagOk = true;
					break;
				} else {
					count++;
				}
			}
			if (flagOk) {
				mapReturn = messageHandler.handler(responseData);
				TaxiInfo taxiInfo = (TaxiInfo) mapReturn.get(Integer.toString(GPSCommand.GPS_TAXI_MONITER));
				returnMap.put("taxiPlateNumber", taxiInfo.getTaxiPlateNumber());
				returnMap.put("driverName", taxiInfo.getDriverName());
				returnMap.put("dirverPhoneNumber", taxiInfo.getDirverPhoneNumber());
				returnMap.put("longitude", taxiInfo.getLongitude());
				returnMap.put("latitude", taxiInfo.getLatitude());
				returnMap.put("gpsTime", taxiInfo.getGpsTime());
				returnMap.put("company", taxiInfo.getCompanyInfo().getName());
				returnMap.put("speed", taxiInfo.getSpeed());
				returnMap.put("monitorCount", applyInfo.getMonitorCount());
				map.put("state", "0");
				map.put("message", "OK");
				map.put("date", new Date());
				map.put("data", returnMap);
			} else {
				map.put("state", "1");
				map.put("message", "ER");
				map.put("date", new Date());
				map.put("data", null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			map.put("state", "1");
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

	/**
	 * 周边查询数据转化
	 * 
	 * @param taxis
	 * @return
	 */
	private List<Map<String, Object>> convertMap(List<TaxiInfo> taxis) {
		List<Map<String, Object>> returnList = new ArrayList<Map<String, Object>>();
		for (TaxiInfo taxi : taxis) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("id", taxi.getId());
			map.put("taxiPlateNumber", taxi.getTaxiPlateNumber());
			map.put("driverName", taxi.getDriverName());
			map.put("dirverPhoneNumber", taxi.getDirverPhoneNumber());
			map.put("breakPromiseCount", taxi.getBreakPromiseCount());
			map.put("companyId", taxi.getCompanyId());
			map.put("isEmpty", taxi.getIsEmpty());
			map.put("speed", taxi.getSpeed());
			map.put("longitude", taxi.getLongitude());
			map.put("latitude", taxi.getLatitude());
			map.put("gpsTime", taxi.getGpsTime());
			map.put("createDate", taxi.getCreateDate());
			map.put("updateDate", taxi.getUpdateDate());
			map.put("companyInfo", taxi.getCompanyInfo());
			map.put("callApplyInfos", null);
			returnList.add(map);
		}
		return returnList;
	}
}
