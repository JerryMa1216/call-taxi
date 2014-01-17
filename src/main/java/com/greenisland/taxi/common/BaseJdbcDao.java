package com.greenisland.taxi.common;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Component;

/**
 * 
 * @author Jerry
 * @E-mail jerry.ma@bstek.com
 * @version 2013-8-22下午12:57:28
 */
@Component("tx.baseJdbcDao")
public class BaseJdbcDao extends JdbcDaoSupport {
	@Autowired
	BaseJdbcDao(DataSource dataSource) {
		setDataSource(dataSource);
	}
}
