package com.greenisland.taxi.push.request;

import com.greenisland.taxi.push.http.HttpMethodName;
import com.greenisland.taxi.push.util.Constants;

/**
 * @author liyazhou@baidu.com
 *
 */
public class QueryDeviceTypeRequest extends BaiduPushRequest{
	public QueryDeviceTypeRequest(String channelId) {
		super.setHttpMethod(HttpMethodName.POST);
		super.setMethodName(Constants.query_device_type);
		
		super.setChannelId(channelId);
	}
}
