package com.example.ordercheck.dao;


import com.example.ordercheck.entity.CpoeOrderCheckRule;

import java.sql.SQLException;
import java.util.List;

/**
 * 医嘱校验规则表
 */
public class CpoeOrderCheckRuleDAO {

    private CpoeOrderCheckRuleDAO(){}
    private static class SingletonHolder{
        private static CpoeOrderCheckRuleDAO intstance = new CpoeOrderCheckRuleDAO();
    }
    public static CpoeOrderCheckRuleDAO getInstance(){
        return CpoeOrderCheckRuleDAO.SingletonHolder.intstance;
    }

    public List<CpoeOrderCheckRule> selectAllRule() throws Exception {
        return null;
    }

    public List<CpoeOrderCheckRule> selectAllAvailableRule() throws Exception {
        return null;
    }

    public int insert(List<CpoeOrderCheckRule> list) throws SQLException {
        return 0;
    }
}
