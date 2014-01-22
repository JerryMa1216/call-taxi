package com.greenisland.taxi.manager;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.greenisland.taxi.common.BaseHibernateDao;
import com.greenisland.taxi.common.constant.ApplicationState;
import com.greenisland.taxi.common.constant.ResponseState;
import com.greenisland.taxi.common.constant.TradeState;
import com.greenisland.taxi.domain.CallApplyInfo;
import com.greenisland.taxi.domain.CommentInfo;

/**
 * 叫车请求service
 * 
 * @author Jerry
 * @E-mail jerry.ma@bstek.com
 * @version 2013-10-20下午3:33:08
 */
@Component("callApplyInfoService")
public class CallApplyInfoService extends BaseHibernateDao {

	/**
	 * 根据订单id查询订单信息
	 * 
	 * @param id
	 * @return
	 */
	public CallApplyInfo getCallApplyInfoById(String id) {
		return this.getHibernateTemplate().get(CallApplyInfo.class, id);
	}

	@SuppressWarnings("unchecked")
	public CallApplyInfo getCallApplyInfo(String id) {
		String hql = "from CallApplyInfo c where c.state ='1' and c.deleteFlag ='N' and c.id=?";
		List<CallApplyInfo> list = this.getHibernateTemplate().find(hql.toString(), id);
		return list != null && list.size() > 0 ? list.get(0) : null;
	}
	@SuppressWarnings("unchecked")
	public CallApplyInfo getCallApplyInfoNoResponse(String id) {
		String hql = "from CallApplyInfo c where c.state ='1' and c.deleteFlag ='N' and c.responseState ='2' and c.id=?";
		List<CallApplyInfo> list = this.getHibernateTemplate().find(hql.toString(), id);
		return list != null && list.size() > 0 ? list.get(0) : null;
	}

	/**
	 * 查询复杂条件订单信息
	 * 
	 * @param id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public CallApplyInfo getApplyInfoValidated(String id) {
		StringBuilder hql = new StringBuilder("from CallApplyInfo c where 1=1 ");
		// 订单未响应
		hql.append("and c.responseState ='" + ResponseState.WAIT_RESPONSE + "' ");
		// 订单未删除，申请状态为有效
		hql.append("and c.deleteFlag ='N' and c.state='" + ApplicationState.VALIDATION + "' ");
		hql.append("and c.id =?");
		List<CallApplyInfo> list = this.getHibernateTemplate().find(hql.toString(), id);
		return list != null && list.size() > 0 ? list.get(0) : null;
	}

	/**
	 * 根据用户id读取所有订单信息
	 * 
	 * @param uid
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<CallApplyInfo> queryApplyInfoByUid(String uid) {
		StringBuilder hql = new StringBuilder("from CallApplyInfo c where 1=1 ");
		hql.append("and c.deleteFlag ='N' and c.state='" + ApplicationState.VALIDATION + "' and c.responseState='" + ResponseState.RESPONSED + "' ");
		hql.append("and c.userId=? order by c.callTime desc");
		List<CallApplyInfo> list = this.getHibernateTemplate().find(hql.toString(), uid);
		return list;
	}

	/**
	 * 读取出租车所有订单评分总数
	 * 
	 * @param taxiId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public int getNiceCount(String taxiId) {
		StringBuilder hql = new StringBuilder("from CallApplyInfo c where 1=1 ");
		hql.append("and c.deleteFlag = 'N' and c.state='" + ApplicationState.VALIDATION + "' and c.isGetOn='1' and c.tradeState='"
				+ TradeState.FINISHED + "' ");
		hql.append("and c.taxiId=?");
		List<CallApplyInfo> list = this.getHibernateTemplate().find(hql.toString(), taxiId);
		int niceCount = 0;
		for (CallApplyInfo apply : list) {
			List<CommentInfo> comments = this.getHibernateTemplate().find("from CommentInfo c where c.applyId=?", apply.getId());
			if (comments != null && comments.size() > 0) {
				int commentLevel = comments.get(0).getLevel() != null ? comments.get(0).getLevel() : 0;
				if(commentLevel < 4){
					niceCount = niceCount + commentLevel;
				}
			}
		}
		return niceCount;
	}

	/**
	 * 根据出租车id查询出租车成交订单数
	 * 
	 * @param taxiId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<CallApplyInfo> getApplyInfoByTaxiId(String taxiId) {
		StringBuilder hql = new StringBuilder("from CallApplyInfo c where 1=1 ");
		hql.append("and c.deleteFlag = 'N' and c.state='" + ApplicationState.VALIDATION + "' and c.isGetOn='1' and c.tradeState='"
				+ TradeState.FINISHED + "' ");
		hql.append("and c.taxiId=?");
		List<CallApplyInfo> list = this.getHibernateTemplate().find(hql.toString(), taxiId);
		return list;
	}

	/**
	 * 保存订单
	 * 
	 * @param applyInfo
	 * @return
	 */
	@Transactional
	public String saveCallApplyInfo(CallApplyInfo applyInfo) {
		return (String) this.getHibernateTemplate().save(applyInfo);
	}

	/**
	 * 更新订单
	 * 
	 * @param applyInfo
	 */
	@Transactional
	public void updateApplyInfo(CallApplyInfo applyInfo) {
		this.getHibernateTemplate().update(applyInfo);
	}

	/**
	 * 删除单车请求
	 * 
	 * @param applyInfo
	 */
	public void deleteApplyInfo(CallApplyInfo applyInfo) {
		this.getHibernateTemplate().delete(applyInfo);
	}
}
