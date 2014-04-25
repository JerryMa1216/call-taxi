package com.greenisland.taxi.manager;

import java.util.List;

import org.springframework.stereotype.Component;

import com.greenisland.taxi.common.BaseHibernateDao;
import com.greenisland.taxi.domain.BlacklistInfo;

@Component("blackListService")
public class BlackListService extends BaseHibernateDao {

	@SuppressWarnings("unchecked")
	public boolean isBlacklist(String phoneNumber) {
		String hql = "from BlacklistInfo b where b.phoneNumber = ?";
		List<BlacklistInfo> list = this.getHibernateTemplate().find(hql, phoneNumber);
		return list != null && list.size() > 0 ? true : false;
	}
}
