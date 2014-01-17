package com.greenisland.taxi.manager;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.greenisland.taxi.common.BaseHibernateDao;
import com.greenisland.taxi.domain.UserInfo;

/**
 * 
 * @author Jerry
 * @E-mail jerry.ma@bstek.com
 * @version 2013-10-20下午3:19:02
 */
@Component
public class UserInfoService extends BaseHibernateDao {

	/**
	 * 根据uid读取用户对象
	 * 
	 * @param id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public UserInfo getUserInfoById(String id) {
		List<UserInfo> list = this.getHibernateTemplate().find("from UserInfo u where u.id =?", id);
		return list.size() > 0 && list != null ? list.get(0) : null;
	}

	/**
	 * 根据手机号读取用户信息
	 * 
	 * @param phoneNumber
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public UserInfo getUserInfoByPhoneNumber(String phoneNumber) {
		List<UserInfo> list = this.getHibernateTemplate().find("from UserInfo u where u.phoneNumber =?", phoneNumber);
		return list.size() > 0 && list != null ? list.get(0) : null;
	}

	/**
	 * 保存用户信息
	 * 
	 * @param userInfo
	 * @return
	 */
	@Transactional
	public String saveUserInfo(UserInfo userInfo) {
		return (String) this.getHibernateTemplate().save(userInfo);
	}

	/**
	 * 更新用户信息
	 * 
	 * @param userInfo
	 */
	@Transactional
	public void updateUserInfo(UserInfo userInfo) {
		this.getHibernateTemplate().update(userInfo);
	}
}
