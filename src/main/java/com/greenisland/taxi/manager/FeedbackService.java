package com.greenisland.taxi.manager;

import org.springframework.stereotype.Component;

import com.greenisland.taxi.common.BaseHibernateDao;
import com.greenisland.taxi.domain.FeedBack;

@Component("feedbackService")
public class FeedbackService extends BaseHibernateDao {
	public void save(FeedBack feedback) {
		this.getHibernateTemplate().save(feedback);
	}
}
