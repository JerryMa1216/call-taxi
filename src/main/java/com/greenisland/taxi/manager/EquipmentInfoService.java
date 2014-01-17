package com.greenisland.taxi.manager;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.greenisland.taxi.common.BaseHibernateDao;
import com.greenisland.taxi.domain.EquipmentInfo;

/**
 * 
 * @author Jerry
 * @E-mail jerry.ma@bstek.com
 * @version 2013-10-20下午3:29:11
 */
@Component("equipmentInfoService")
public class EquipmentInfoService extends BaseHibernateDao {
	@SuppressWarnings("unchecked")
	public EquipmentInfo getEquipmentById(String id) {
		List<EquipmentInfo> list = this.getHibernateTemplate().find("from EquipmentInfo e where e.equipmentId =?", id);
		return list.size() > 0 && list != null ? list.get(0) : null;
	}

	@Transactional
	public void update(EquipmentInfo equipmentInfo) {
		this.getHibernateTemplate().update(equipmentInfo);
	}

	@Transactional
	public String save(EquipmentInfo equipmentInfo) {
		return (String) this.getHibernateTemplate().save(equipmentInfo);
	}
}
