package com.greenisland.taxi.gateway.gps.resolver;

import java.util.Map;

/**
 * 
 * @author Jerry
 * @E-mail jerry.ma@bstek.com
 * @version 2013-10-26下午2:24:47
 */
public interface MessageHandler {
	public Map<String, Object> handler(String message) throws Exception;
}
