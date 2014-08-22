package com.greenisland.taxi.security.filter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.util.AntPathMatcher;

import com.greenisland.taxi.domain.UserInfo;
import com.greenisland.taxi.manager.UserInfoService;
import com.greenisland.taxi.security.utils.DES;

/**
 * 安全过滤
 * 
 * @author Jerry
 * @E-mail jerry.ma@bstek.com
 * @version 2013-10-20下午3:42:36
 */
public class AuthenticateFilter implements Filter {
	private static Logger log = Logger.getLogger(AuthenticateFilter.class.getName());
	private UserInfoService userInfoService;
	protected AntPathMatcher rrm = new AntPathMatcher();
	protected String ignoreURL = null;
	protected ArrayList<String> alIgnoreURL = new ArrayList<String>();

	public void setUserInfoService(UserInfoService userInfoService) {
		this.userInfoService = userInfoService;
	}

	public void setIgnoreURL(String ignoreURL) {
		this.ignoreURL = ignoreURL;
	}

	public void init(FilterConfig filterConfig) throws ServletException {
		StringTokenizer tokenizer = new StringTokenizer(ignoreURL, ",");
		while (tokenizer.hasMoreElements()) {
			alIgnoreURL.add(tokenizer.nextElement().toString());
		}
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HashMap<String, Object> map = new HashMap<String, Object>();
		ObjectMapper objectMapper = new ObjectMapper();
		HttpServletRequest req = (HttpServletRequest) request;
		response.reset();
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/json");
		PrintWriter pw = response.getWriter();
		String url = req.getRequestURI().toString();
		try {
			if (!isIgnoreURL(url, req.getContextPath())) {
				String sign = request.getParameter("sign");
				String uid = request.getParameter("uid");
				UserInfo user = this.userInfoService.getUserInfoById(uid);
				try {
					String decryptSign = DES.decryptDES(sign, user.getKey());
					String token = decryptSign.split(",")[0];
					if (!user.getToken().trim().equals(token)) {
						log.error("来自：" + request.getRemoteAddr() + "用户访问资源" + url + "没有访问权限！");
						map.put("state", "3");
						map.put("message", "无访问权限");
						pw.write(objectMapper.writeValueAsString(map));
						pw.flush();
						pw.close();
						return;
					}
				} catch (Exception e) {
					e.printStackTrace();
					map.put("state", "3");
					map.put("message", "无访问权限");
					pw.write(objectMapper.writeValueAsString(map));
					pw.flush();
					pw.close();
					return;
				}
			}
			chain.doFilter(request, response);
		} catch (ServletException e) {
			e.printStackTrace();
			log.error(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			log.error(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
		}
	}

	public void destroy() {
		this.ignoreURL = null;
	}

	/**
	 * 该方法用于判断该请示url是否在权限忽略控制的列表中,如果在权限忽略控制的列表中,则返回真.
	 */
	public boolean isIgnoreURL(String url, String contextPath) {
		for (int i = 0; i < this.alIgnoreURL.size(); i++) {
			if (url.equals(contextPath + "/")) {
				return true;
			}
			String pattern = contextPath + this.alIgnoreURL.get(i);
			if (rrm.match(pattern, url)) {
				return true;
			}
		}
		return false;
	}

}
