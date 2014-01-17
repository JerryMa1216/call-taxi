package com.greenisland.taxi.push.request;

import com.greenisland.taxi.push.http.HttpMethodName;
import com.greenisland.taxi.push.util.Constants;

/**
 * @author liyazhou@baidu.com
 *
 */
public class QueryUserTagsRequest extends UserRelatedRequest{
	
	public QueryUserTagsRequest(String userId) {
		super.setHttpMethod(HttpMethodName.POST);
		super.setMethodName(Constants.query_user_tags);
		super.setUserId(userId);
	}
}


