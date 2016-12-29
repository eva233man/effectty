package com.sitech.billing.effectty.support.mdb.transaction;

import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;

import java.io.IOException;
import java.io.Reader;

/**
 * Created by wangyla on 2016/3/22.
 */
public class SqlMapClientDaoSupport {
    private static SqlMapClient sqlMapClient = null;


    static {
        Reader reader = null;
        try {
            reader = Resources.getResourceAsReader("SqlMapConfig.xml");
            sqlMapClient = SqlMapClientBuilder.buildSqlMapClient(reader);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public SqlMapClient getInstance() {
        return sqlMapClient;
    }


}
