package com.greenisland.taxi.test;

import java.util.Arrays;
import java.util.UUID;

import org.junit.Test;

import com.greenisland.taxi.push.DefaultPushClient;
import com.greenisland.taxi.push.auth.PushCredentials;
import com.greenisland.taxi.push.model.DeviceType;
import com.greenisland.taxi.push.model.MessageType;
import com.greenisland.taxi.push.model.PushType;
import com.greenisland.taxi.push.request.PushMessageRequest;
import com.greenisland.taxi.push.response.PushResponse;

public class PushTest {
	private final static String API_KEY = "Hh3TibjsDoMZWi2NxXGRVGF6";
	private final static String SECRIT_KEY = "moeDFfZMpbSNoRaH1hDpNfzSLHWf1BM9";

	// @Test
	// public void pushNotify() {
	// // 40286881439b3e9d01439b3f002f0002
	// DefaultPushClient client = new DefaultPushClient(new
	// PushCredentials(API_KEY, SECRIT_KEY));
	// PushMessageRequest request = new PushMessageRequest();
	// request.setMessageType(MessageType.notify);
	// StringBuilder message = new StringBuilder("{\"aps\":{\"alert\":");
	// message.append("\"159打车：您的订单已被抢到\",\"sound\":\"\",\"badge\":\"\"},");
	// message.append("\"applyId\":\"4028688143b5943e0143b5ad9b17000d\",");
	// message.append("\"cType\":\"1\"}");
	// request.setMessages(message.toString());
	// request.setChannelId("5426366355066971952");
	// request.setUserId("1035429303757798159");
	// request.setPushType(PushType.single_user);
	// request.setDeviceTypes(Arrays.asList(DeviceType.iso));
	// request.setDeployStatus(Long.valueOf(2));
	// request.setMessageKeys("msgkeys");
	// PushResponse<Integer> response = client.pushMessage(request);
	// System.out.println(response);
	// }

	// @Test
	// public void pushMessageAndroid() {
	// DefaultPushClient client = new DefaultPushClient(new
	// PushCredentials(API_KEY, SECRIT_KEY));
	// PushMessageRequest request = new PushMessageRequest();
	// request.setMessageType(MessageType.message);
	// StringBuilder message = new StringBuilder("{\"custom\":{");
	// message.append("\"id\":\"4028688142e7e29c0142e7eda2ab000b\",");
	// message.append("\"plate\":\"浙A123\",");
	// message.append("\"name\":\"顾师傅\",");
	// message.append("\"tel\":\"1231231231\",");
	// message.append("\"c\":\"159公司\",");
	// message.append("\"nice\":\"10\",");
	// message.append("\"order\":\"10\",");
	// message.append("\"cType\":\"1\",");
	// message.append("\"lo\":\"121.634877\",");
	// message.append("\"la\":\"31.219655\"}");
	// message.append("}");
	// request.setChannelId("4478218280303304392");
	// request.setUserId("745409507009129179");
	// request.setMessages(message.toString());
	// request.setPushType(PushType.single_user);
	// request.setDeviceTypes(Arrays.asList(DeviceType.android));
	// request.setDeployStatus(Long.valueOf(2));
	// request.setMessageKeys(UUID.randomUUID().toString());
	// PushResponse<Integer> response = client.pushMessage(request);
	// System.out.println(response);
	// }

}
