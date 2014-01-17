package com.greenisland.taxi.manager;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.greenisland.taxi.common.BaseHibernateDao;
import com.greenisland.taxi.domain.TaxiInfo;

/**
 * 
 * @author Jerry
 * @E-mail jerry.ma@bstek.com
 * @version 2013-10-20下午3:20:17
 */
@Component("taxiInfoService")
public class TaxiInfoService extends BaseHibernateDao {
	/**
	 * 根据id获取出租车信息
	 * 
	 * @param id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public boolean validateTaxiExist(String taxiPlateNumber) {
		String hql = "from TaxiInfo taxi where taxi.taxiPlateNumber=?";
		List<TaxiInfo> list = this.getHibernateTemplate().find(hql, taxiPlateNumber);
		return list != null && list.size() > 0;
	}

	@SuppressWarnings("unchecked")
	public TaxiInfo getTaxiByPlateNumber(String plateNumber) {
		String hql = "from TaxiInfo taxi where taxi.taxiPlateNumber =?";
		List<TaxiInfo> list = this.getHibernateTemplate().find(hql, plateNumber);
		return list != null && list.size() > 0 ? list.get(0) : null;
	}

	public TaxiInfo getTaxiInfoById(String id) {
		return this.getHibernateTemplate().get(TaxiInfo.class, id);
	}

	@Transactional
	public void updateTaxiInfo(TaxiInfo taxiInfo) {
		this.getHibernateTemplate().update(taxiInfo);
	}

	@Transactional
	public String saveTaxiInfo(TaxiInfo taxiInfo) {
		return (String) this.getHibernateTemplate().save(taxiInfo);
	}
}
