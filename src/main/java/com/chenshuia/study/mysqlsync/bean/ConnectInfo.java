package com.chenshuia.study.mysqlsync.bean;

import com.chenshuia.study.mysqlsync.util.VerifyUtil;
import com.mysql.cj.jdbc.Driver;
import lombok.Data;

@Data
public class ConnectInfo {
    /*
    数据库链接信息
     */
    private String driver = Driver.class.getName();
    private String url ;
    private String userName ;
    private String password ;

    public void verify(){
        VerifyUtil.verifyString(url,userName,password,driver);
    }

}
