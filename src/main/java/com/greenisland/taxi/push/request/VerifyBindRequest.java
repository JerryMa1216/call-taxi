package com.greenisland.taxi.push.request;

import com.greenisland.taxi.push.http.HttpMethodName;
import com.greenisland.taxi.push.util.Constants;

/**
 * @author liyazhou@baidu.com
 *
 */
public class VerifyBindRequest extends UserRelatedRequest{
	public VerifyBindRequest(String userId) {
		super.setHttpMethod(HttpMethodName.POST);
		super.setMethodName(Constants.verify_bind);
		super.setUserId(userId);
	}
}
