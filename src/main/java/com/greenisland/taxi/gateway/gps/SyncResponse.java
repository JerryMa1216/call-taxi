package com.greenisland.taxi.gateway.gps;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.greenisland.taxi.common.constant.GPSCommand;
import com.greenisland.taxi.common.constant.MechineType;
import com.greenisland.taxi.common.constant.ResponseState;
import com.greenisland.taxi.domain.CallApplyInfo;
import com.greenisland.taxi.domain.CompanyInfo;
import com.greenisland.taxi.domain.TaxiInfo;
import com.greenisland.taxi.gateway.push.PushClient;
import com.greenisland.taxi.manager.CallApplyInfoService;
import com.greenisland.taxi.manager.CompanyInfoService;
import com.greenisland.taxi.manager.TaxiInfoService;

@Component
public class SyncResponse {
	@Resource
	private CallApplyInfoService callApplyInfoService;
	@Resource
	private CompanyInfoService companyInfoService;
	@Resource
	private TaxiInfoService taxiInfoService;
	@Resource
	private PushClient pushClient;

	public synchronized void handlerResponse(String responseData) {
		Map<String, Object> mapTaxi = null;// 调用接口返回值
		mapTaxi = handler(responseData);
		if (mapTaxi != null) {
			int niceCount;
			int orderCount;
			String applyId = (String) mapTaxi.get("applyId");
			String[] ids = applyId.split("-");
			String mechineType = ids[1];
			String userId = ids[2];
			String channelId = ids[3];
			String callType = ids[4];
			TaxiInfo respTaxi = (TaxiInfo) mapTaxi.get(Integer.toString(GPSCommand.GPS_TAXI_RESP));
			CallApplyInfo applyInfo = callApplyInfoService.getApplyInfoValidated(ids[0]);
			CompanyInfo respCompany = respTaxi.getCompanyInfo();
			if (applyInfo != null) {
				CompanyInfo company = companyInfoService.getCompanyByName(respCompany != null ? respCompany.getName() : null);
				String taxiId = null;
				String companyId = null;
				TaxiInfo taxi = new TaxiInfo();
				// 判断公司是否存在
				if (company != null) {
					taxi = taxiInfoService.getTaxiByPlateNumber(respTaxi.getTaxiPlateNumber());
					if (taxi == null) {
						respTaxi.setCompanyId(company.getId());
						respTaxi.setBreakPromiseCount(0);
						respTaxi.setCreateDate(new Date());
						taxiId = taxiInfoService.saveTaxiInfo(respTaxi);
					} else {
						taxiId = taxi.getId();
					}
				} else {
					respCompany.setId(UUID.randomUUID().toString());
					respCompany.setCreateDate(new Date());
					companyId = companyInfoService.saveCompany(respCompany);
					taxi = taxiInfoService.getTaxiByPlateNumber(respTaxi.getTaxiPlateNumber());
					if (taxi == null) {
						respTaxi.setCompanyId(companyId);
						respTaxi.setBreakPromiseCount(0);
						respTaxi.setCreateDate(new Date());
						taxiId = taxiInfoService.saveTaxiInfo(respTaxi);
					} else {
						taxiId = taxi.getId();
					}
				}
				// 更新订单信息
				applyInfo.setTaxiId(taxiId);
				// 更新订单响应状态为“已响应” 1
				applyInfo.setResponseState(ResponseState.RESPONSED);
				applyInfo.setUpdateDate(new Date());
				callApplyInfoService.updateApplyInfo(applyInfo);
				// 根据出租车id查询订单信息
				List<CallApplyInfo> applies = callApplyInfoService.getApplyInfoByTaxiId(taxiId);
				orderCount = applies != null && applies.size() > 0 ? applies.size() : 0;
				niceCount = callApplyInfoService.getNiceCount(taxiId);
				// 调用推送
				if (mechineType.equals(MechineType.ANDROID)) {
					pushClient.pushSinglerUserAndroid(company.getName(), niceCount, orderCount, userId, channelId, ids[0], callType, respTaxi);
				} else {
					pushClient.pushSingleUserIOS(userId, channelId, ids[0],callType);
				}
			}
		}
	}

	private Map<String, Object> handler(String message) {
		
		Map<String, Object> map = new HashMap<String, Object>();
		if (StringUtils.hasText(message)) {
			String msg1 = message.substring(2);
			String msg2 = msg1.substring(0, msg1.indexOf(">"));
			// 消息id
			// String msgId = msg2.substring(0, 4);
			// 流水id
			// String processId = msg2.substring(5, 15);
			// 消息体
			String body = msg2.substring(16);
			String[] content = body.split(",");
			String respBody = new String();

			String applyId = content[0];
			TaxiInfo respTaxi = new TaxiInfo();
			CompanyInfo respCompany = new CompanyInfo();
			respBody = content[1];
			String[] respTaxis = respBody.split("\\|");
			respTaxi.setTaxiPlateNumber(respTaxis[0]);
			respTaxi.setLongitude(respTaxis[1]);
			respTaxi.setLatitude(respTaxis[2]);
			respTaxi.setGpsTime(respTaxis[3]);
			respTaxi.setSpeed(respTaxis[4]);
			respTaxi.setIsEmpty(respTaxis[5]);
			respTaxi.setDriverName(respTaxis[7]);
			respTaxi.setDirverPhoneNumber(respTaxis[8]);
			respCompany.setName(respTaxis[9]);
			respTaxi.setCompanyInfo(respCompany);
			map.put(Integer.toString(GPSCommand.GPS_TAXI_RESP), respTaxi);
			map.put("applyId", applyId);
			return map;
		}
		return null;
	}
}
