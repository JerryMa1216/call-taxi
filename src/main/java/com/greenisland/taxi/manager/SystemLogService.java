package com.greenisland.taxi.manager;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.greenisland.taxi.common.BaseHibernateDao;
import com.greenisland.taxi.domain.SystemLog;

@Component("systemLogService")
public class SystemLogService extends BaseHibernateDao {
	@Transactional
	public void save(SystemLog log) {
		this.getHibernateTemplate().save(log);
	}
}
