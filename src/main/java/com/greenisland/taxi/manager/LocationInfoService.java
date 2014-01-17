package com.greenisland.taxi.manager;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.greenisland.taxi.common.BaseHibernateDao;
import com.greenisland.taxi.domain.LocationInfo;

/**
 * 
 * @author Jerry
 * @E-mail jerry.ma@bstek.com
 * @version 2013-10-20下午3:21:10
 */
@Component("locationInfoService")
public class LocationInfoService extends BaseHibernateDao {
	/**
	 * 保存位置信息
	 * 
	 * @param locationInfo
	 */
	@Transactional
	public void saveLocationInfo(LocationInfo locationInfo) {
		this.getHibernateTemplate().save(locationInfo);
	}
}
