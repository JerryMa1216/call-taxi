package com.greenisland.taxi.manager;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.greenisland.taxi.common.BaseHibernateDao;
import com.greenisland.taxi.domain.CompanyInfo;

/**
 * 公司信息维护
 * 
 * @author Jerry
 * @E-mail jerry.ma@bstek.com
 * @version 2013-10-20下午3:30:45
 */
@Component("companyInfoService")
public class CompanyInfoService extends BaseHibernateDao {
	@Transactional
	public String saveCompany(CompanyInfo company) {
		return (String) this.getHibernateTemplate().save(company);
	}

	@SuppressWarnings("unchecked")
	public CompanyInfo getCompanyByName(String name) {
		String hql = "from CompanyInfo c where c.name =?";
		List<CompanyInfo> list = this.getHibernateTemplate().find(hql, name);
		return list != null && list.size() > 0 ? list.get(0) : null;
	}

	@SuppressWarnings("unchecked")
	public CompanyInfo getCompanyById(String id) {
		String hql = "from CompanyInfo c where c.id =?";
		List<CompanyInfo> list = this.getHibernateTemplate().find(hql, id);
		return list != null && list.size() > 0 ? list.get(0) : null;
	}

	@Transactional
	public void updateCompany(CompanyInfo companyInfo) {
		this.getHibernateTemplate().update(companyInfo);
	}
}
