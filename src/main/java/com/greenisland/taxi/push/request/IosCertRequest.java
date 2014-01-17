package com.greenisland.taxi.push.request;

import com.greenisland.taxi.push.http.HttpMethodName;
import com.greenisland.taxi.push.model.IosCert;

/**
 * @author liyazhou@baidu.com
 *
 */
public class IosCertRequest extends BaiduPushRequest{
	private IosCert isoCert;
	
	public IosCertRequest() {}
	
	public IosCertRequest(IosCert IosCert) {
		super.setHttpMethod(HttpMethodName.POST);
		this.isoCert = IosCert;
	}

	public IosCert getIsoCert() {
		return isoCert;
	}

	public void setIsoCert(IosCert isoCert) {
		this.isoCert = isoCert;
	}
}
