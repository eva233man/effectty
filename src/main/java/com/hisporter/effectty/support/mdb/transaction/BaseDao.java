package com.hisporter.effectty.support.mdb.transaction;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by wangyla on 2016/3/22.
 */
public class BaseDao extends SqlMapClientDaoSupport {
	public Object queryForObject(String statementName) throws SQLException {
		return super.getInstance().queryForObject(statementName);
	}

	public Object queryForObject(String statementName, Object parameterObject) throws SQLException {
		return super.getInstance().queryForObject(statementName, parameterObject);
	}

	@SuppressWarnings("rawtypes")
	public List queryForList(String statementName) throws SQLException {
		return super.getInstance().queryForList(statementName);
	}

	@SuppressWarnings("rawtypes")
	public List queryForList(String statementName, Object parameterObject) throws SQLException {
		return super.getInstance().queryForList(statementName, parameterObject);
	}
	
}
