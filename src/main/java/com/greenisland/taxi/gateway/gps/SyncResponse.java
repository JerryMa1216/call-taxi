package com.greenisland.taxi.gateway.gps;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
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

@Component("syncResponse")
public class SyncResponse {
	private static Logger log = Logger.getLogger(SyncResponse.class);
	@Resource
	private CallApplyInfoService callApplyInfoService;
	@Resource
	private CompanyInfoService companyInfoService;
	@Resource
	private TaxiInfoService taxiInfoService;
	@Resource
	private PushClient pushClient;

	@Transactional
	public void handlerResponse(String responseData, GpsClient gpsClient) {
		try {
			Map<String, Object> mapTaxi = null;// 调用接口返回值
			mapTaxi = handler(responseData);
			if (mapTaxi != null) {
				int niceCount;
				int orderCount;
				String applyId = (String) mapTaxi.get("applyId");
				String[] ids = applyId.split("-");
				// 设备类型
				String mechineType = ids[1];
				// 百度云推送userId
				String userId = ids[2];
				// 百度云推送channelId
				String channelId = ids[3];
				// 叫车类型
				String callType = ids[4];
				TaxiInfo respTaxi = (TaxiInfo) mapTaxi.get(Integer.toString(GPSCommand.GPS_TAXI_RESP));
				// ids[0]申请id
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
							// 出租车存在则更新出租车坐标
						} else {
							// 更新出租车坐标
							taxi.setLongitude(respTaxi.getLongitude());
							taxi.setLatitude(respTaxi.getLatitude());
							taxiInfoService.updateTaxiInfo(taxi);
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
							// 更新出租车坐标
							taxi.setLongitude(respTaxi.getLongitude());
							taxi.setLatitude(respTaxi.getLatitude());
							taxiInfoService.updateTaxiInfo(taxi);
							taxiId = taxi.getId();
						}
					}
					// 更新订单信息
					applyInfo.setTaxiId(taxiId);
					applyInfo.setDirverPhoneNumber(respTaxi.getDirverPhoneNumber());
					// 更新订单响应状态为“已响应” 1
					applyInfo.setResponseState(ResponseState.RESPONSED);
					applyInfo.setUpdateDate(new Date());
					callApplyInfoService.updateApplyInfo(applyInfo);
					// 根据出租车id查询订单信息
					List<CallApplyInfo> applies = callApplyInfoService.getApplyInfoByTaxiId(taxiId);
					orderCount = applies != null && applies.size() > 0 ? applies.size() : 0;
					// 好评数
					niceCount = callApplyInfoService.getNiceCount(taxiId);
					// 调用推送
					if (mechineType.equals(MechineType.ANDROID)) {
						pushClient.pushSinglerUserAndroid(company != null ? company.getName() : null, niceCount, orderCount, userId, channelId,
								ids[0], callType, respTaxi);
					} else {
						pushClient.pushSingleUserIOS(userId, channelId, ids[0], callType);
					}
				}
			}
			// 执行完推送后关闭socket
			log.info("========== 司机抢答成功，关闭socket==========");
			gpsClient.cancel();
		} catch (Exception e) {
			log.error(e.getMessage());
			gpsClient.cancel();
		}

	}

	private Map<String, Object> handler(String message) throws Exception {

		Map<String, Object> map = new HashMap<String, Object>();
		if (StringUtils.hasText(message)) {
			String msg1 = message.substring(2);
			String msg2 = msg1.substring(0, msg1.indexOf(">"));
			String body = msg2.substring(16);
			String[] content = body.split(",");
			String respBody = new String();
			// 订单申请id
			String applyId = content[0];
			TaxiInfo respTaxi = new TaxiInfo();
			CompanyInfo respCompany = new CompanyInfo();
			// 响应消息体
			respBody = content[1];
			// 拆分为字符串数组
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
