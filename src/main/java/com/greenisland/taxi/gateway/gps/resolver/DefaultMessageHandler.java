package com.greenisland.taxi.gateway.gps.resolver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.greenisland.taxi.common.constant.GPSCommand;
import com.greenisland.taxi.domain.CompanyInfo;
import com.greenisland.taxi.domain.TaxiInfo;

/**
 * 
 * @author Jerry
 * @E-mail jerry.ma@bstek.com
 * @version 2013-10-26下午2:25:28
 */
@Component
public class DefaultMessageHandler implements MessageHandler {
	private static Log log = LogFactory.getLog(DefaultMessageHandler.class);
	
	public Map<String, Object> handler(String message) {
		Map<String, Object> map = new HashMap<String, Object>();
		if (StringUtils.hasText(message)) {
			String msg1 = message.substring(2);
			String msg2 = msg1.substring(0, msg1.indexOf(">"));
			// 消息id
			String msgId = msg2.substring(0, 4);
			// 流水id
//			String processId = msg2.substring(5, 15);
			// 消息体
			String body = msg2.substring(16);
			String[] content = body.split(",");
			String respBody = new String();
			switch (Integer.parseInt(msgId)) {
			case GPSCommand.GPS_LOGIN:
				log.info("login success!");
				break;
			case GPSCommand.GPS_HEARTBEAT:
				log.info("heart beat success!");
				break;
			case GPSCommand.GPS_AROUND_TAXIS:
				List<TaxiInfo> list = new ArrayList<TaxiInfo>();
				// 周边查询有车辆信息
				if (content[0].equals("OK") && Integer.parseInt(content[1]) > 0) {
					for (int i = 2; i < content.length; i++) {
						TaxiInfo taxi = new TaxiInfo();
						CompanyInfo company = new CompanyInfo();
						respBody = content[i];
						String[] taxis = respBody.split("\\|");
						taxi.setTaxiPlateNumber(taxis[0]);
						taxi.setLongitude(taxis[1]);
						taxi.setLatitude(taxis[2]);
						taxi.setGpsTime(taxis[3]);
						taxi.setSpeed(taxis[4]);
						taxi.setIsEmpty(taxis[5]);
						taxi.setDriverName(taxis[7]);
						taxi.setDirverPhoneNumber(taxis[8]);
						company.setName(taxis[9]);
						taxi.setCompanyInfo(company);
						list.add(taxi);
					}
					map.put(Integer.toString(GPSCommand.GPS_AROUND_TAXIS), list);
				} else {
					map.put(Integer.toString(GPSCommand.GPS_AROUND_TAXIS), null);
				}
				break;
			case GPSCommand.GPS_TAXI_MONITER:
				TaxiInfo taxi = new TaxiInfo();
				CompanyInfo company = new CompanyInfo();
				respBody = content[1];
				String[] taxis = respBody.split("\\|");
				taxi.setTaxiPlateNumber(taxis[0]);
				taxi.setLongitude(taxis[1]);
				taxi.setLatitude(taxis[2]);
				taxi.setGpsTime(taxis[3]);
				taxi.setSpeed(taxis[4]);
				taxi.setIsEmpty(taxis[5]);
				taxi.setDriverName(taxis[7]);
				taxi.setDirverPhoneNumber(taxis[8]);
				company.setName(taxis[9]);
				taxi.setCompanyInfo(company);
				map.put(Integer.toString(GPSCommand.GPS_TAXI_MONITER), taxi);
				break;
			case GPSCommand.GPS_CALL_RESP:
				if(content[1].equals("OK")){
					map.put(Integer.toString(GPSCommand.GPS_CALL_RESP), content[0]);
				}else{
					map.put(Integer.toString(GPSCommand.GPS_CALL_RESP), "ER");
				}
				break;
			case GPSCommand.GPS_TAXI_RESP:
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
				break;
			}
		}
		return map;
	}

}
