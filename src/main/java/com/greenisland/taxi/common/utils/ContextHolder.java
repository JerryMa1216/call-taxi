package com.greenisland.taxi.common.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ContextHolder implements ApplicationContextAware {
	private static ApplicationContext applicationContext = null;

	/**
	 * 根据给出的beanId来获取在Spring当中配置的bean
	 * 
	 * @param beanId
	 *            给出的beanId
	 * @return 返回找到的bean对象
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getSpringBean(String beanId) {
		return (T) getSpringBeanFactory().getBean(beanId);
	}

	/**
	 * 取得WebApplicationContext对象.
	 * <p>
	 * 根据web.xml中对的配置顺序,在Spring启动完成后可用.
	 * </p>
	 * 
	 * @return 返回当前应用Spring的WebApplicationContext对象
	 */
	public static ApplicationContext getSpringBeanFactory() {
		return applicationContext;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		// TODO Auto-generated method stub
		// 仅服务于web环境.
		ContextHolder.applicationContext = applicationContext;
	}
}
