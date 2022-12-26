package com.chenshuia.study.mysqlsync.common;

import com.chenshuia.study.mysqlsync.bean.ConnectInfo;
import org.apache.ibatis.datasource.pooled.PooledDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

public class LocalDataSourceFactory {

    private Map<String,DataSource> dataSourceMap;

    private LocalDataSourceFactory(){
        dataSourceMap = new HashMap<>();
    }
    // 构造器，私有，不能被new

    // ClassHolder属于静态内部类，在加载类Demo03的时候，只会加载内部类ClassHolder，
    // 但是不会把内部类的属性加载出来
    private static class ClassHolder{
        // 这里执行类加载，是jvm来执行类加载，它一定是单例的，不存在线程安全问题
        // 这里不是调用，是类加载，是成员变量
        private static final LocalDataSourceFactory holder =new LocalDataSourceFactory();

    }

    public static LocalDataSourceFactory of(){//第一次调用getInstance()的时候赋值
        return ClassHolder.holder;
        }

    public DataSource getPoolDataSource(ConnectInfo connectInfo){
        String key = getDataSourceKey(connectInfo);
        if (dataSourceMap.containsKey(key)){
            return dataSourceMap.get(key);
        }
        PooledDataSource dataSource = new PooledDataSource();
        dataSource.setUrl(connectInfo.getUrl());
        dataSource.setUsername(connectInfo.getUserName());
        dataSource.setPassword(connectInfo.getPassword());
        dataSource.setDriver(connectInfo.getDriver());
        dataSourceMap.put(key,dataSource);
        return dataSource;

    }

    private String getDataSourceKey(ConnectInfo connectInfo){
        return String.format("%s-%s-%s",connectInfo.getUrl(),connectInfo.getUserName(),connectInfo.getPassword());

    }

}
