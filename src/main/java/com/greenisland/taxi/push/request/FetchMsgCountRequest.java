package com.greenisland.taxi.push.request;

import com.greenisland.taxi.push.http.HttpMethodName;
import com.greenisland.taxi.push.util.Constants;

/**
 * @author liyazhou@baidu.com
 *
 */
public class FetchMsgCountRequest extends UserRelatedRequest{
	public FetchMsgCountRequest(String userId) {
		super.setHttpMethod(HttpMethodName.POST);
		super.setMethodName(Constants.fetch_msgcount);
		super.setUserId(userId);
	}
}


