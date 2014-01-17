package com.greenisland.taxi.push.request;

import java.util.ArrayList;
import java.util.List;

import com.greenisland.taxi.push.http.HttpMethodName;
import com.greenisland.taxi.push.util.Constants;

/**
 * @author liyazhou@baidu.com
 *
 */
public class DeleteMsgRequest extends UserRelatedRequest{
	private List<String> msgIds = new ArrayList<String>();
	
	private DeleteMsgRequest(){
		super.setHttpMethod(HttpMethodName.POST);
		super.setMethodName(Constants.delete_msg);
	}
	
	public DeleteMsgRequest(String userId,String msgId) {
		this();
		super.setUserId(userId);
		addMsgId(msgId);
	}
	
	public DeleteMsgRequest(String userId,List<String> msgIds) {
		this();
		super.setUserId(userId);
		this.setMsgIds(msgIds);
	}
	
	public void addMsgId(String id){
		msgIds.add(id);
	}

	public List<String> getMsgIds() {
		return msgIds;
	}

	public void setMsgIds(List<String> msgIds) {
		this.msgIds = msgIds;
	}
}


